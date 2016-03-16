# Kernel

Kernel is a Docker based CDI container which allows a Java application to be built and deployed within the Docker environment.

All they need to do is to deploy their jar's within the /opt/kernel/lib/ directory and they will then get deployed.

## Using this image

First you need to create your CDI application as usual. Any beans that you want to be loaded when the application starts you need to add an observer, that's described below.

Next create a docker file and put your jar's into the /opt/kernel/lib/`artifactId` directory. You could put them directly under lib but this allows you to deploy
multiple applications into one container.

A good example is our [Fileserver](https://github.com/peter-mount/fileserver) application. There you will see the relevant pom.xml, Dockerfile and assembly.xml
that are required to build your application's image.

### Generic Docker file
Here's an example docker file you can use to create a new application:
```
FROM area51/kernel:latest

ADD myapplication.tar /opt/kernel/lib/myapplication
```

There's no need to add CMD etc as they are inherited, although you can add whatever you want to the image.

### Opendata Docker file
As this project is mainly used for the opendata project, there's a second image available which also includes our core CDI modules like configuration support etc.

```
FROM area51/kernel:latest-opendata

ADD myapplication.tar /opt/kernel/lib/myapplication
```

## Starting an application bean

For an application to start you need to tell the bootstrap to load a CDI bean on startup and you're application will load.
To do this you simply need to observe the `onl.area51.kernel.CommandArguments` interface

First you need the following dependency added to maven:
```xml
<dependency>
    <groupId>onl.area51.kernel</groupId>
    <artifactId>kernel-core</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

Next your core application bean, for example:

```java
package my.application;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import onl.area51.kernel.CommandArguments;

@ApplicationScoped
public class MyApplication {

    void boot( @Observes CommandArguments args )
    {
        // Nothing to do here, it's presence ensures the bean is instantiated by CDI
    }

    @PostConstruct
    void start()
    {
        // Optional, will be called when the application is shutdown
    }

    @PreDestroy
    void stop()
    {
        // Optional, will be called when the application is shutdown
    }
}
```

When the application starts then this bean will be instantiated. If a method is annotated with `@PostConstruct` then it will be called, then the method with
`@Observes CommandArguments`. At that point your bean is loaded and running.

On shutdown any method annotated with `@PreDestroy` will be invoked as normal.

### Command line arguments
Normally there won't be any as this is invoked from within the docker environment, but any command line arguments (i.e. think `public static void main(String[] args)`)
will be in the `CommandArguments` instance which you can access using `List<String> getArguments();`.

## Running your application

You run your application just like any other docker container. Although part of another project a pre-built example would be:
```
docker run -it --rm area51/fileserver
```
This will download and run our fileserver container which is based on this project. Press ctrl-C to shut it down.

## Environment variables
The following variables are supported by the Kernel which in most instances you won't need to use. Your application can add to these

variable | default | description
-------- | ------- | -----------
KERNEL_LIB | /opt/kernel | The location under which the application's jar files are located
KERNEL_DEBUG | | If set to true then additional debugging is included in the logs.

## Directory layout within the container

directory/file | contents
--------- | --------
/opt/kernel/kernel.jar | The kernel bootstrap
/opt/kernel/logging.properties | The logging configuration specific for docker
/opt/kernel/start | Shell script to start the kernel
/opt/kernel/cdilib | jars forming the CDI container. This is Weld but could be replaced by any CDI2 based container.
/opt/kernel/lib | your application jars go into subdirectories under here
/opt/kernel/sharedlib | common jars. The opendata image places its jars under here
/opt/kernel/ext | optional extension jars

## Available images

image | content
----- | -------
area51/kernel | latest generic image
area51/kernel:latest | latest generic image
area51/kernel:latest-opendata | latest opendata image

You can see the entire list on [Docker Hub](https://hub.docker.com/r/area51/kernel/tags/)

## History

Originally this was a simple runtime which was part of the [OpenData](https://github.com/peter-mount/opendata) project and based on it's
[opendata-kernel](https://github.com/peter-mount/opendata-common/tree/master/opendata-kernel) module.

This version is similar to that but customized to run only within a docker environment.
