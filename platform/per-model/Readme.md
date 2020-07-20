# Peregrine Sling Model Annotations

## Image Info

The @ImageInfo annotation marks a given string property as the container for image data like
**width and height**.

### Setup

1. Add a **String property** to your Sling Model that will contain the info
2. Add these annotations
    1. @Inject
    2. @ImageInfo
3. Add the field name of the image source to the **name** property of the @ImageInfo annotation

### Effect

If an image has these nodes: **jcr:content -> metadata -> per-data** the data will be stored in the
String property as: "{'width': 123, 'height': 234}".
If no image data was found then the Image Info property is null.

## Externalize Path

The path of an Image can be externalized so that it will reflect the way the users sees the nodes and
not how they are stored in the JCR tree.

### Setup

1. Add **ExternalizePathSerializer** as Json Serializer with:
```java
@JsonSerialize(using = ExternalizePathSerializer.class)
```
2.  Add **@ExternalizePath** to the image path field or getter method
```java
@ExternalizePath
public String getImagePath() {
    return imagePath;
}
```

### Effect

The image source property is now changed so that it matches the user's view.