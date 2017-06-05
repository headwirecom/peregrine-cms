package com.peregrine.admin.data;

import java.util.Calendar;

/**
 * Created by schaefa on 6/2/17.
 */
public interface PerPageManager {

    public PerPage getPage(String pagePath);

    public void touch(PerPage page, boolean shallow, Calendar now, boolean clearReplication);
}
