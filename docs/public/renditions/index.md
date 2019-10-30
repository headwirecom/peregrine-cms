peregrine cms renditions
=====

# Introduction

Peregrine supports Asset Renditions out of the box but it requires
that **VIPS** is installed in order to make it work. VIPS is supported
on Windows, Mac and Linux.

# Installing VIPS

Unless you are running the official Peregrine Docker image, VIPS needs to 
be installed manually as it requires installation of native OS code. 
To learn more on how to install **libvips**:

[libvips Wiki Page](https://github.com/jcupitt/libvips/wiki)

For Mac OS X **Homebrew** seems to be the easiest installation
(https://github.com/jcupitt/libvips/wiki/Build-for-macOS).

## Linux (Ubuntu)

If you have an apt-get based Linux system, run the following command:


```
$ sudo apt-get install libvips libvips-dev libvips-tools -y
```

## Mac OS X

**Homebrew** seems to be the easiest installation
(https://github.com/jcupitt/libvips/wiki/Build-for-macOS).

## Windows

TODO

## Validate VIPS Installation

Please make sure htat VIPS is working before more on:

    vips --version

which should yield something like this:

    vips-8.5.5-Tue May 16 10:44:23 BST 2017

**Attention**: as of 7/26/2019 Peregrine will just bypass the image if
VIPS is not installed instead of failing. With that the Image Transformations
are enabled by default and ready to use w/o any configuration except the
Generic Image Transformation which needs a configuration as there are no
good defaults.

# Short Introduction

An Image Rendition is done with this:
```
/content/assets/test.png.rendition.json/thumbnail.png
```
The path to the asset is appended with **.rendition.json/&lt;IT Setup>**.
The **IT Setup** name is then used to find the IT Configuration
which provides the name to the **Image Transformation** (IT), an optional
path to limit the application of the IT and a list of parameters to be
provided to the IT.
The **path** will limit to which assets the rendition can be applied to.
The most common application is to have multiple IT Setups that are applied
to different parts of the JCR tree. For example if there are different
customer having their own sub folder with assets then an IT Setup can be
created which gives each customer their own renditions.
**IT Setup** can chain **ITs** to combine the image transformation. For
example the Greyscale and Thumbnail IT can be used to make a greyscaled
thumbnail rendition of an image.
The **Image Transformation** is a class that encapsulates the execution of
the underlying image handling commands like **VIPS**. It defines a name
which is referenced in the **IT Setup** and some IT specific parameters.
The **IT** services that cannot be executed due to missing dependencies
like **VIPS** are handing down the image unchanged and **will not store**
the image in the asset's renditions node.

# Image Transformation Structure

The basic image handling is done by the **Image Transformation** which are referenced by their
name. That should start with a prefix like **vips** to keep them apart from other image transformation
services added in the future. These classes are what is either executing or calling the image
handling programs. They **must be enabled** for VIPS in order to be used otherwise the rendition
will fail.

On the other hand we have the **Image Transformation Setup**s which are the configuration of the
image handling for a particular purpose like creating a Greyscale Thumbnail in PNG format. Each
of them are referenced by its name and the extension of the name indicate the target image type
like **.png** or **.jpeg**. Each Setup must provide a least one **Configuration** which contains
the name of the **Image Transformation** and optional parameters.

Under the hood the **Image Transformation Setup** provides a list of **Image Transformation Configuration**s
which is then used to obtain the **Image Transformation** from the **Image Transformation Provider**.
Both together (Image Transformation and Image Transformation Configuration) are then used to
configure and execute the image handling.

# Image Transformation Configuration

Peregrine provides these Image Transformation Services:

* Thumbnail: creates a smaller version the original image with or without cropping
* Greyscale: creates a grey image of the original image
* Convert: just converts an image from a given type to another
* Generic: enables the configurator to setup a wide variety of image transformations
           not covered here 

### Thumbnail Configuration

|Name|Parameter|Required|Type|Default|Description|
|:---|:--------|:-------|:---|:------|:----------|
|Enabled|enabled|yes|boolean|true|Flag to indicate if the Service can be used or not|
|Name|name|yes|String|vips:thumbnail|Name of the Image Transformation used for the Setup|
|Default Width|defaultWidth|no|int|50|Default Width in pixels of the target image|
|Default Height|defaultHeight|no|int|50|Default Height in pixels of the target image|

![Image Transformation Configuration for Thumbnail](renditions.image.transformation.configuration.thumbnail.png)

### Greyscale Configuration

|Name|Parameter|Required|Type|Default|Description|
|:---|:--------|:-------|:---|:------|:----------|
|Enabled|enabled|yes|boolean|false|Flag to indicate if the Service can be used or not|
|Name|name|yes|String|vips:true|Name of the Image Transformation used for the Setup|

![Image Transformation Configuration for Greyscale](renditions.image.transformation.configuration.greyscale.png)

### Convert Configuration

|Name|Parameter|Required|Type|Default|Description|
|:---|:--------|:-------|:---|:------|:----------|
|Enabled|enabled|yes|boolean|true|Flag to indicate if the Service can be used or not|
|Name|name|yes|String|vips:greyscale|Name of the Image Transformation used for the Setup|

![Image Transformation Configuration for Convert](renditions.image.transformation.configuration.convert.png)

### Generic Configuration

|Name|Parameter|Required|Type|Default|Description|
|:---|:--------|:-------|:---|:------|:----------|
|Enabled|enabled|yes|boolean|true|Flag to indicate if the Service can be used or not|
|Name|name|yes|String|vips:greyscale|Name of the Image Transformation used for the Setup|
|Command|command|yes|String|generic|Name of the command|
|Command Line|cli|no|String[]|..|List of Parameters with or without placeholders|

**Rules for Placeholders**:
1. A placeholder is enclosed by double curly brackets: **{{placeholder}}**
2. A parameter name can be added to the placeholder to make it conditional like
   **{{--test-param||test}}** in which parameter **--test-param** is only added to
   the command when value for **test** is provided in IT Setup which is added as second parameter
3. A parameter can be added as prefix or suffix to a placeholder: **--test-this={{test}}**
   which will become a single parameter if **test** is provided in IT Setup and that
   value is then filled in
4. Options 2. and 3. cannot be mixed. It is either one of the other

![Image Transformation Configuration for Convert](renditions.image.transformation.configuration.generic.png)

# Image Transformation Setup

Image Transformations are the basic building blocks but the Image Transformation Setup is combining the
Image Transformation into the final rendition actions. They also define the output format with its extension
as well as the actual parameters of the image transformation if provided.
For a single Image Transformation you still need to create an Image Transformation Setup just with a single
Image Transformation. That said you can create multiple Image Transformation Setups for various image types
or image sizes etc.

The configuration of an Image Transformation Setup: **com.peregrine.transform.ImageTransformationSetup**:

|Name|Parameter|Required|Type|Default|Description|
|:---|:--------|:-------|:---|:------|:----------|
|Name|name|yes|String|none|Name of the Image Transformation Setup. You need to provide an extension for the target image type|
|Configuration|imageTransformationConfiguration|yes|String|none|List of Image Transformation in the format: transformation=&lt;transformation name>[\|parameter name=parameter value]*|

For example if there is a need for a greyscale thumbnail you can execute a greyscale and then a thumbnail
to generated the desired image.

![Image Transformation Setup Confiuration for Greyscale Thumbnail](renditions.image.transformation.setup.configuration.greyThumbnail.png)

And this is an example of the Generic **vipsthumbnail** IT Setup that is using **vipsthumbnail**
instead of **vips thumbnail** which size and optional cropping:

![Image Transformation Setup Confiuration for Greyscale Thumbnail](renditions.image.transformation.setup.configuration.vipsthumbnailCrop.png)

and this is the same but without cropping:

![Image Transformation Setup Confiuration for Greyscale Thumbnail](renditions.image.transformation.setup.configuration.vipsthumbnail.png)


# Renditions

Renditions are created and stored under the asset node's **renditions** node which the name of the
image transformation setup. The API call to obtain (and create a rendition if not already there) is

    &Lt;URL to the Asset without extension>.rendition.json/&lt;image transformation name>

So to create an thumbnail on the asset **/content/assets/test.png** you use the following URL:

    /content/assets/test.png.rendition.json/thumbnail.png

**GET** and **POST** HTTP Methods are supported for this call and work the same way.
