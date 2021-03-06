package com.dubture.composer.core.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IAccessRule;
import org.eclipse.dltk.core.IBuildpathAttribute;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.php.internal.core.buildpath.BuildPathUtils;

@SuppressWarnings("restriction")
public class BuildpathUtil
{
    
    public static void setupVendorBuildpath(IScriptProject scriptProject, IProgressMonitor progress) throws ModelException
    {
        IProject project = scriptProject.getProject();
        IPath composerPath = project.getFullPath().append("vendor");
        IBuildpathEntry[] rawBuildpath = scriptProject.getRawBuildpath();
        progress.setTaskName("Setting vendor buildpath...");

        for (IBuildpathEntry entry : rawBuildpath) {
            if (entry.getPath().equals(composerPath)) {
                BuildPathUtils.removeEntryFromBuildPath(scriptProject, entry);
            }
        }
        
        BuildPathUtils.addEntriesToBuildPath(scriptProject, getVendorEntries(composerPath));
        progress.worked(1);
    }
    
    protected static List<IBuildpathEntry> getVendorEntries(IPath composerPath)
    {
        
        IPath[] include = new IPath[]{new Path("composer/*")};
        IBuildpathAttribute[] attributes = new IBuildpathAttribute[0];
        IPath[] exclude = new IPath[0];
        IBuildpathEntry vendorEntry = DLTKCore.newBuiltinEntry(
                composerPath, new IAccessRule[0], attributes,
                include, exclude, false, false);
        List<IBuildpathEntry> vendorEntries = new ArrayList<IBuildpathEntry>();
        vendorEntries.add(vendorEntry);
        
        return vendorEntries;
        
    }
}
