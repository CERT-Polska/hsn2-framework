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

package pl.nask.hsn2.framework.workflow.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.UnmergedPathException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * This class is thread safe
 */
public class GitWorkflowRepository implements WorkflowRepository {

	private static class GitFilenameFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			return !".git".equals(name);
		}
	}

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GitWorkflowRepository.class);
	private FileRepository repo;
	private File repoDir;
	private FilenameFilter filenameFilter = new GitFilenameFilter();

	public GitWorkflowRepository(String repositoryPath, boolean forceCreate)
			throws WorkflowRepoException {

		repoDir = new File(repositoryPath);
		ensureDirExists(repoDir, forceCreate);
		ensureDirIsWritable(repoDir);

		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			// expecting repositoryPath to be exact, so 'findGitDir()' is not required here
			repo = builder.setWorkTree(repoDir).findGitDir(repoDir).build();

			if (forceCreate && !repo.getConfig().getFile().exists() && checkRepoMightBeCreated(repoDir)) {
				LOGGER.debug("Workflow repository structure does't exist, trying to create one...");
				repo.create();
			}
			if (!repo.getConfig().getFile().exists()) {
				throw new WorkflowRepoException("Workflow repository structure doesn't exist and cannot be created or is not forced.");
			}
			validateGitRepo();
		} catch (IOException e) {
			throw new WorkflowRepoException("Couldn't create repository for path: " + repoDir.getAbsolutePath(), e);
		}
	}

	/**
	 * Checks if repository directory is writable.
	 * 
	 * @param directory
	 *            Directory file to be checked.
	 * @throws WorkflowRepoException
	 *             If directory is not writable the exception will thrown.
	 */
	private void ensureDirIsWritable(File directory)
			throws WorkflowRepoException {
		if (!directory.canWrite()) {
			throw new WorkflowRepoException(
					"Workflow repository is not writable, path: "
							+ directory.getAbsolutePath());
		}
	}

	private boolean checkRepoMightBeCreated(File dir)
			throws WorkflowRepoException {
		if (dir.isFile())
			return false;
		if (dir.listFiles().length != 0)
			throw new WorkflowRepoException(
					"Directory is not empty, Git repository cannot be created in: "
							+ dir.getAbsolutePath());
		return true;
	}

	private void validateGitRepo() throws WorkflowRepoException, IOException {
		Status stat = null;
		try {
			stat = newGit().status().call();
		} catch (NoWorkTreeException e) {
			throw new WorkflowRepoException(e.getMessage(), e);
		}
		if (!stat.isClean()) {
			throw new WorkflowRepoException(
					"Git repository is not clean, so is invalid");
		}
	}

	/**
	 * Checks if repository directory exists. Recreates it if
	 * <code>forceCreate</code> is <code>true</code>.
	 * 
	 * @param directory
	 *            Directory of the repository.
	 * @param forceCreate
	 *            Force create directory if doesn't exist.
	 * @throws WorkflowRepoException
	 *             If there is a problem with creation directory, the exception
	 *             will thrown.
	 */
	private void ensureDirExists(File directory, boolean forceCreate)
			throws WorkflowRepoException {
		if (directory.exists()) {
			return;
		}
		LOGGER.debug("Repository doesn't exist, trying to create it...");
		if (forceCreate && !directory.mkdirs()) {
			throw new WorkflowRepoException(
					"Workflow repository cannot be created for path: "
							+ directory.getAbsolutePath());
		}
		if (!directory.exists()) {
			throw new WorkflowRepoException(
					"Workflow repository desn't exist and is not forced to be created. Path: "
							+ directory.getAbsolutePath());
		}
	}

	@Override
	public List<String> listWorkflowNames() throws WorkflowRepoException {
		File[] files = repoDir.listFiles(filenameFilter);
		List<String> l = new ArrayList<String>(files.length);
		try {
			ObjectId revId = repo.resolve(Constants.HEAD);
			if (revId == null) {
				throw new WorkflowRepoException(
						"GIT repository does not exists or it does not contains any workflow files.");
			}
			TreeWalk tree = new TreeWalk(repo);
			tree.addTree(new RevWalk(repo).parseTree(revId));
			while (tree.next()) {
				l.add(tree.getNameString());
			}
		} catch (IOException ex) {
			throw new WorkflowRepoException(
					"Error during listing GIT repository.", ex);
		}
		return l;
	}

	@Override
	public WorkflowVersionInfo saveWorkflow(String workflowName, InputStream is)
			throws WorkflowRepoException {
		saveFile(workflowName, is);
		return addToRepo(workflowName);
	}

	private WorkflowVersionInfo addToRepo(String workflowName)
			throws WorkflowRepoException {
		try {
			newGit().add().addFilepattern(workflowName).call();
			return commit("Workflow saved: " + workflowName);
		} catch (Exception e) {
			LOGGER.error("Error adding file ({})" + workflowName
					+ " to the local repo ({}) : {}", new Object[] {
					workflowName, repoDir.getAbsolutePath(), e.getMessage() });
			LOGGER.error("Exception while adding file to the local repo", e);
			throw new WorkflowRepoException(
					"Exception while adding file to the local repo", e);
		}
	}

	private WorkflowVersionInfo commit(String message) throws NoHeadException,
			NoMessageException, UnmergedPathException,
			ConcurrentRefUpdateException, JGitInternalException,
			WrongRepositoryStateException {
		RevCommit res = newGit().commit().setMessage(message).call();
		return new GitVersionInfo(res);
	}

	private void saveFile(String workflowName, InputStream is)
			throws WorkflowRepoException {
		File f = new File(repoDir, workflowName);
		FileOutputStream os = null;
		try {
			if (!f.exists()) {
				f.createNewFile();
			}
			os = new FileOutputStream(f);
			IOUtils.copy(is, os);
		} catch (IOException e) {
			LOGGER.error("Error saving file ({})" + workflowName
					+ " in the working directory ({}) : {}", new Object[] {
					workflowName, repoDir.getAbsolutePath(), e.getMessage() });
			LOGGER.error(
					"Exception while saving worflow in the working directory",
					e);
			throw new WorkflowRepoException(
					"Exception while saving worflow in the working directory",
					e);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);
		}
	}

	@Override
	public List<WorkflowVersionInfo> getVersions(String workflowName)
			throws WorkflowRepoException {
		try {
			LogCommand cmd = newGit().log();
			cmd.addPath(workflowName);
			Iterable<RevCommit> res = cmd.call();

			List<WorkflowVersionInfo> list = new ArrayList<WorkflowVersionInfo>();
			for (RevCommit rc : res) {
				list.add(new GitVersionInfo(rc));
			}

			return list;
		} catch (Exception e) {
			LOGGER.error("Error listing commits for {}", workflowName);
			throw new WorkflowRepoException("Error listing commits for "
					+ workflowName, e);
		}
	}

	@Override
	public InputStream getWorkflowFile(String workflowName, String version)
			throws WorkflowRepoException {
		checkout(workflowName, version);
		File f = new File(repoDir, workflowName);
		try {
			return new FileInputStream(f);
		} catch (FileNotFoundException e) {
			throw new WorkflowRepoException("No such file: " + workflowName,
					null);
		}

	}

	private void checkout(String workflowName, String version)
			throws WorkflowRepoException {
		CheckoutCommand cmd = newGit().checkout();
		cmd.addPath(workflowName);
		cmd.setStartPoint(version);

		try {
			cmd.call();
		} catch (Exception e) {
			LOGGER.error("Error checking out {}, rev {}", workflowName,
					version == null ? "HEAD" : version);
			throw new WorkflowRepoException("Error performing checkout"
					+ workflowName, e);
		}
	}

	private Git newGit() {
		return new Git(repo);
	}
}
