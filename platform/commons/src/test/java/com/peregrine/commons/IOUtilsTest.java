package com.peregrine.commons;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public final class IOUtilsTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File dir;

    @Before
    public void setUp() throws IOException {
        dir = temporaryFolder.newFolder("dir");
        temporaryFolder.newFile("dir/file.txt");
    }

    @Test
    public void createChildDirectory() {
        assertNull(IOUtils.createChildDirectory(dir));
        assertNull(IOUtils.createChildDirectory(dir, (String[]) null));
        assertNull(IOUtils.createChildDirectory(dir, "file.txt"));
        final File child = IOUtils.createChildDirectory(this.dir, "file.txt", "child");
        assertNotNull(child);
        assertEquals("child", child.getName());
    }

    @Test
    public void deleteFileOrDirectory() {
        assertTrue(IOUtils.deleteFileOrDirectory(dir));
    }

}
