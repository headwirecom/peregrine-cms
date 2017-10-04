package com.peregrine.nodejs.process;

import java.io.Reader;

/**
 * Interface for a External Process Execution
 *
 * Created by Andreas Schaefer on 4/5/17.
 */
public interface ProcessContext {

    public static final int NO_EXIT_CODE = -99999;

    /** @return Exit Code of the Process **/
    public int getExitCode();

    /** @return Output of the Process as Text **/
    public String getOutput();

    /** @return Reader providing the Process Output **/
    public Reader getOutputReader();

    /** @return Error Message of a failed Process **/
    public String getError();

    /** @return Reader for the Error Message **/
    public Reader getErrorReader();

    /** finalizes any resources opened during usage of this instance **/
    public void tearDown();
}
