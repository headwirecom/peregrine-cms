package com.peregrine.admin.process;

import java.io.Reader;

/**
 * Created by schaefa on 4/5/17.
 */
public interface ProcessContext {

    public static final int NO_EXIT_CODE = -99999;

    public int getExitCode();

    public String getOutput();

    public Reader getOutputReader();

    public String getError();

    public Reader getErrorReader();

    public void tearDown();
}
