package com.peregrine.intra;

import org.apache.sling.api.resource.ResourceResolver;

public interface IntraSlingCaller {
    public CallerContext createContext();
    public byte[] call(CallerContext callerContext) throws CallException;

    interface CallerContext {
        public ResourceResolver getResourceResolver();
        public CallerContext setResourceResolver(ResourceResolver resourceResolver);
        public String getPath();
        public CallerContext setPath(String path);
        public String getSelectors();
        public CallerContext setSelectors(String selectors);
        public String getExtension();
        public CallerContext setExtension(String extension);
        public String getSuffix();
        public CallerContext setSuffix(String suffix);
        public String getQueryString();
        public CallerContext setQueryString(String queryString);
    }

    class CallException extends Exception {
        public CallException(String message) {
            super(message);
        }

        public CallException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
