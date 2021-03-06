package com.dubture.composer.ui.wizard.importer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.internal.resources.ProjectDescriptionReader;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dltk.utils.ResourceUtil;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.project.PHPNature;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

import com.dubture.composer.core.ComposerNature;
import com.dubture.composer.core.facet.FacetManager;
import com.dubture.composer.core.log.Logger;
import com.dubture.composer.ui.ComposerUIPluginImages;

@SuppressWarnings("restriction")
public class ComposerImportWizard extends Wizard implements IImportWizard {

	private IWorkbench workbench;
	private IStructuredSelection selection;
	private WizardResourceImportPage mainPage;


	public ComposerImportWizard() {
		// TODO Auto-generated constructor stub
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {

		this.workbench = workbench;
		this.selection = currentSelection;
		
		List selectedResources = IDE.computeSelectedResources(currentSelection);
		if (!selectedResources.isEmpty()) {
			this.selection = new StructuredSelection(selectedResources);
		}

		setWindowTitle(DataTransferMessages.DataTransfer_importTitle);
		setDefaultPageImageDescriptor(ComposerUIPluginImages.IMPORT_PROJECT);//$NON-NLS-1$
		setNeedsProgressMonitor(true);
		
	}
	
	public void addPages() {
		super.addPages();
		mainPage = new WizardResourceImportPage(workbench, selection,
				getFileImportMask());
		addPage(mainPage);
	}
	

	@Override
	public boolean performFinish() {
		IRunnableWithProgress op = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IWorkspaceRoot root = workspace.getRoot();
				String projectName = mainPage.getProjectName();
				IProject project = root.getProject(mainPage.getProjectName());
				monitor.beginTask("Importing composer project", 5);
				
				try {
					
					IPath locationPath = new Path(mainPage.getSourcePath());
					IProjectDescription description = null;
					
					if (locationPath.append(".project").toFile().exists()) {
						ProjectDescriptionReader reader = new ProjectDescriptionReader(project);
						description = reader.read(locationPath.append(".project"));
					} else {
						description = workspace.newProjectDescription(projectName);
					}
					
					// If it is under the root use the default location
					if (Platform.getLocation().isPrefixOf(locationPath)) {
						description.setLocation(null);
					} else {
						description.setLocation(locationPath);
					}
					
					monitor.worked(1);
					project.create(description, monitor);
					project.open(monitor);
					monitor.worked(1);
					
					if (!project.hasNature(PHPNature.ID)) {
						ResourceUtil.addNature(project, monitor, PHPNature.ID);
					}
					
					if (!project.hasNature(ComposerNature.NATURE_ID)) {
						ResourceUtil.addNature(project, monitor, ComposerNature.NATURE_ID);
					}
					
					ProjectFacetsManager.create(project);
					
					FacetManager.installFacets(project, PHPVersion.PHP5_4, monitor);
					
					monitor.worked(1);
					
					project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					monitor.worked(2);
					
				} catch (CoreException e) {
					Logger.logException(e);
				} catch (IOException e) {
					Logger.logException(e);
				} finally {
					monitor.done();
				}
			}
		};
		
		try {
			getContainer().run(false, true, op);
		} catch (Exception e) {
			Logger.logException(e);
			return false;
		}
		return true;
	}
	
	protected String[] getFileImportMask() {
		return null;
	}
}
