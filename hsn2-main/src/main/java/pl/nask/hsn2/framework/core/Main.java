/*
 * Copyright (c) NASK, NCSC
 * 
 * This file is part of HoneySpider Network 2.0.
 * 
 * This is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.nask.hsn2.framework.core;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.bus.api.BusException;
import pl.nask.hsn2.bus.api.BusManager;
import pl.nask.hsn2.bus.recovery.RecoveryMonitor;
import pl.nask.hsn2.framework.bus.RbtBusConfiguration;
import pl.nask.hsn2.framework.bus.RbtFrameworkBus;
import pl.nask.hsn2.framework.configuration.Configuration;
import pl.nask.hsn2.framework.configuration.ConfigurationException;
import pl.nask.hsn2.framework.configuration.ConfigurationManager;
import pl.nask.hsn2.framework.configuration.ConfigurationManagerImpl;
import pl.nask.hsn2.framework.configuration.MappingException;
import pl.nask.hsn2.framework.configuration.validation.ValidationException;
import pl.nask.hsn2.framework.workflow.repository.GitWorkflowRepository;
import pl.nask.hsn2.framework.workflow.repository.WorkflowRepoException;
import pl.nask.hsn2.suppressor.SingleThreadTasksSuppressor;
import pl.nask.hsn2.utils.FileIdGenerator;
import pl.nask.hsn2.workflow.engine.ActivitiWorkflowDefinitionManager;
import pl.nask.hsn2.workflow.engine.ActivitiWorkflowEngine;

/**
 * Main class for the HSN2 Framework.
 */
public class Main {

    private static Main instance;
    private Logger logger;
    private ConfigurationManager configManager;
    private static CommandLine commandLineParams;
    private RecoveryMonitor recoveryMonitor;
    private RbtFrameworkBus rbtBus;

	public static void main(String[] args) throws InterruptedException, ConfigurationException {
	    synchronized (Main.class) {
	    	parseCommandLineOptions(args);
	    	if (instance != null) {
	            // restart
	            instance.stop();
	        }
	        instance = new Main();
	        
	        try {
	        	// shutdown hook added
		        Runtime.getRuntime().addShutdownHook(new Thread(){
		        	@Override
		        	public void run() {
		        		instance.stop();
		        	}
		        });
		        
		        instance.start(args);

		        Thread.currentThread().join();
	        } catch (BusException e) {
	        	instance.logger.error("Framework cannot attach to the bus. Is Rabbit MQ working?");
	        	instance.stop();
	        	System.exit(1);
	        } catch (Throwable tw) {
	        	instance.logger.error("Cought: ", tw);
	        	instance.stop();
	        	System.exit(1);
	        }
	    }
	}

	private void start(String[] args) throws ConfigurationException, BusException {
        initLogging();
        initConfiguration();
        initBus();
        initWorkflowManager();
        // should not start BUS until all other components are initialized
        startBus();
        logger.info("Framework started.");
    }

	/**
	 * Stops the framework
	 * @throws InterruptedException
	 */
	private void stop() {

		logger.debug("Stopping framework...");

		try {
	    	// stopping recovery monitor
	    	if (recoveryMonitor != null) {
		    	recoveryMonitor.stop();
		    	recoveryMonitor = null;
		    }
	
	    	// stopping the bus
		    if (rbtBus != null) {
		    	rbtBus.stop();
		    	rbtBus = null;
		    }
		} catch (Exception e) {
			logger.error("There are problems with shutdown the framework, ignoring.", e);
		}

    	logger.info("Framework stopped.");
	}

    private void initWorkflowManager() throws ConfigurationException {
        Configuration configuration = configManager.getCurrentConfig();
        GitWorkflowRepository repo;
        try {
            repo = new GitWorkflowRepository(configuration.getWorkflowRepositoryPath(), true);
        } catch (WorkflowRepoException e) {
            throw new ConfigurationException("Something is wrong with workflow repository. Is the path valid? : " + configuration.getWorkflowRepositoryPath(), e);
        }
    	WorkflowManager.setWorkflowDefinitionManager(new ActivitiWorkflowDefinitionManager());
        WorkflowManager.setKnownServiceNames(configuration.getAMQPServicesNames());
        WorkflowManager.setWorkflowRepository(repo);
        
        // create jobs id generator to be used by the engine
        FileIdGenerator idGenerator = new FileIdGenerator();
        idGenerator.setSequenceFile(configuration.getJobSequenceFile());
        idGenerator.setForceCreate(false);
        if (!idGenerator.seqFileExists()) {
			throw new ConfigurationException(
					"Sequence file doesn't exist. Create it first: "
							+ configuration.getJobSequenceFile()
							+ " or change configuration key 'jobs.sequence.file' to point on correct file.");
        }
        
        // ONLY ONE SUPPRESSOR INSTANCE SHOULD BE USED (not thread safe)
        SingleThreadTasksSuppressor suppressor = new SingleThreadTasksSuppressor();
        
        ActivitiWorkflowEngine engine = new ActivitiWorkflowEngine(idGenerator, suppressor, configuration.getJobsSuppressorTasksThreshold());
        WorkflowManager.setWorkflowEngine(engine);

        // sets limits
        WorkflowManager.setMaximumRunningJobLimit(configuration.getJobsLimit());
        
        WorkflowManager.getInstance();
    }

    @SuppressWarnings("static-access")
	private static void parseCommandLineOptions(String[] args) {
		Options options = new Options();
		options.addOption(OptionBuilder.withLongOpt("logFile").withArgName("file").hasArg().withDescription("use given file for log").create("lf"));
		options.addOption(OptionBuilder.withLongOpt("logLevel").withArgName("level").hasArg().withDescription("use given level for log").create("ll"));
		options.addOption(OptionBuilder.withLongOpt("configPath").withArgName("config").hasArg().withDescription("use given config path").create("cp"));
		options.addOption(OptionBuilder.withLongOpt("help").withDescription("this message").create("h"));
		options.addOption(OptionBuilder.withLongOpt("debug").withDescription("enable queues debug mode (turn off auto ack)").create("d"));
		try {
			commandLineParams = new PosixParser().parse(options, args);
			if (commandLineParams.hasOption("h")) {
				printHelpAndExit(options);
			}
		} catch (ParseException e) {
			System.err.println("Cannot parse command line options");
			printHelpAndExit(options);
		}
	}

    private static void printHelpAndExit(Options options){
    	HelpFormatter formatter = new HelpFormatter();
    	formatter.printHelp("java -jar ...", options );
    	System.exit(0);
    }

	protected void initConfiguration() throws ConfigurationException {
        try {
            ConfigurationManagerImpl mgr = new ConfigurationManagerImpl();
            if (commandLineParams.hasOption("configPath")) {
                mgr.reloadConfig(commandLineParams.getOptionValue("configPath"));
            } else {
                mgr.reloadConfig();
            }
            configManager = mgr;
            FrameworkContext.registerConfigurationManager(mgr);
            logger.info("Configuration loaded");
        } catch (ConfigurationException e) {
            logger.error("Error loading configuration file", e);
            throw e;
        } catch (ValidationException e) {
            logger.error("Configuration file not valid", e);
            throw new ConfigurationException(e);
        } catch (MappingException e) {
            logger.error("Error loading configuration file", e);
            throw new ConfigurationException(e);
        } catch (FileNotFoundException e) {
            logger.error("Error initializing configuration ", e);
            throw new ConfigurationException(e);
        } catch (IOException e) {
            logger.error("Error initializing configuration ", e);
            throw new ConfigurationException(e);
        }
    }

	private void initBus() throws BusException {
	    Configuration cfg = configManager.getCurrentConfig();

	    RbtBusConfiguration busConfig = new RbtBusConfiguration()
	    	.setAMQPServerAddress(cfg.getAMQPServerAddress())
	    	.setAMQPFrameworkLowQueue(cfg.getAMQPFrameworkLowQueue())
	    	.setAMQPFrameworkHighQueue(cfg.getAMQPFrameworkHighQueue())
	    	.setServicesNames(cfg.getAMQPServicesNames())
	    	.setOsLowQueueName(cfg.getAMQPObjectStoreQueueLow())
	    	.setOsHiQueueName(cfg.getAMQPObjectStoreQueueHigh())
	    	.setAmqpExchangeCommonName(cfg.getAMQPExchangeCommon())
	    	.setAmqpExchangeMonitoringName(cfg.getAMQPExchangeMonitoring())
	    	.setAmqpExchangeServicesName(cfg.getAMQPExchangeServices())
	    	.setAmqpConsumersNumber(cfg.getAMQPConsumersNumber());


	    rbtBus = new RbtFrameworkBus(busConfig);
	    rbtBus.initOutgoingConnectors();

		BusManager.setBus(rbtBus);
	}
	
	private void startBus() throws BusException {
		rbtBus.start();
		logger.info("AMQP will be started");

		this.recoveryMonitor = new RecoveryMonitor();
		recoveryMonitor.registerRecoverable(rbtBus);
		recoveryMonitor.start();
	}

	private void initLogging() {
	    if(commandLineParams.hasOption("logfile")){
	        LoggerManager.changeLog4jProperty("log4j.appender.PRIMARY.file", commandLineParams.getOptionValue("logfile"));
	    }
	    if(commandLineParams.hasOption("debuglevel")){
	        LoggerManager.changeLogLevel(commandLineParams.getOptionValue("debuglevel"));
	    }
	    logger = LoggerFactory.getLogger(Main.class);
	}

    public static void stopFramework() throws InterruptedException {
        if (instance != null)
            instance.stop();
    }
}