## Client-Side Testing Configuration

This Bundle provides the configurations for the Client Side Testing.

Because this is a bundle OSGi Service Configuration must be provided as **JSon** files
as XML is not supported. These are the rules:

1. Regular JSon Object
1. 'jcr:primaryType' but be set to **sling:OsgiConfig**
1. Properties are added a attributes
1. Numeric values are added as JSon numbers like **"myNumber":123**
1. Boolean values are added as JSon booleans like **"myBoolean":false**
1. Multi-Values are added as JSon value arrays like **"myList":["one", "two", "three"]**
1. **No Comments or Multi-Line Strings** in JSon

