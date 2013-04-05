/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.team.build.internal.hjplugin.tests;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.jvnet.hudson.test.HudsonTestCase;

import com.google.common.io.Files;
import com.ibm.team.build.internal.hjplugin.RTCFacadeFactory;
import com.ibm.team.build.internal.hjplugin.RTCFacadeFactory.RTCFacadeWrapper;

public class RTCFacadeFactoryIT extends HudsonTestCase {

	@Override
	protected void setUp() throws Exception {
		if (Config.DEFAULT.isConfigured()) {
			super.setUp();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		if (Config.DEFAULT.isConfigured()) {
			super.tearDown();
		}
	}

	public void testGetFacade() {
		if (Config.DEFAULT.isConfigured()) {
			try {
				RTCFacadeFactory.getFacade(Config.DEFAULT.getToolkit(), null);
			} catch (Exception e) {
				Assert.fail("Toolkit not found at " + Config.DEFAULT.getToolkit());
			}
		}
	}
	
	public void testGetFacadeSwitch() throws IOException {
		if (Config.DEFAULT.isConfigured()) {
			RTCFacadeWrapper facade = null;
			try {
				facade = RTCFacadeFactory.getFacade(Config.DEFAULT.getToolkit(), null);
				
				// ask again for the same one, should get the same one back
				RTCFacadeWrapper facade2 = RTCFacadeFactory.getFacade(Config.DEFAULT.getToolkit(), null);
				if (facade != facade2) {
					Assert.fail("Cached toolkit was not reused " + facade + " second requested facade " + facade2);
				}
			} catch (Exception e) {
				Assert.fail("Toolkit not found at " + Config.DEFAULT.getToolkit());
			}

			File srcDir = new File(Config.DEFAULT.getToolkit());
			File destDir = null;

			try {
				
				// create a build toolkit copy
				destDir = Files.createTempDir();
				FileUtils.copyDirectory(srcDir, destDir);

				try {
					
					// create a new facade with the build toolkit copy
					RTCFacadeWrapper facade3 = RTCFacadeFactory.getFacade(destDir.getAbsolutePath(), null);
					if (facade == facade3) {
						Assert.fail("Cached toolkit re-used for different toolkit path " + facade + " new toolkit facade " + facade3);
					}
					
					// get the new facade with the build toolkit copy and make sure it is the cached version
					RTCFacadeWrapper facade4 = RTCFacadeFactory.getFacade(destDir.getAbsolutePath(), null);
					if (facade3 != facade4) {
						Assert.fail("Cached toolkit was not reused " + facade3 + " second requested facade " + facade4);
					}
					
					// get the new facade with the build toolkit copy with a trailing slash and make sure it is the cached version
					facade4 = RTCFacadeFactory.getFacade(destDir.getAbsolutePath() + File.separator, null);
					if (facade3 != facade4) {
						Assert.fail("Cached toolkit was not reused " + facade3 + " second requested facade " + facade4);
					}

				} catch (Exception e) {
					Assert.fail("Failed swtiching to toolkit " + destDir);
				}
			} finally {
				if (destDir != null) {
					
					// delete the build toolkit copy
					FileUtils.deleteDirectory(destDir);
				}
			}
			
			try {
				// ask for the original toolkit - it should still be cached since default size is 3
				RTCFacadeWrapper facade2 = RTCFacadeFactory.getFacade(Config.DEFAULT.getToolkit(), null);
				if (facade != facade2) {
					Assert.fail("Cached toolkit was not reused " + facade + " second requested facade " + facade2);
				}
			} catch (Exception e) {
				Assert.fail("Toolkit not found at " + Config.DEFAULT.getToolkit());
			}
		}
	}
}
