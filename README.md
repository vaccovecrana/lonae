# myrmica

A lightweight Maven dependency resolver.

## Overview

[`myrmica`](https://en.wikipedia.org/wiki/Myrmica) allows you to resolve dependency graphs for Maven artifacts and
repositories from within your client code.

*Quick disclaimer*: To make the best use of yor time (and mine):

> This project, although functional, is still in an experimental stage. If you
> are expecting to get artifacts resolved in the exact same way Maven
> or Gradle would, then `myrmica` is not for you. At least not until solid
> support for runtime dependency semantic quirks is established/implemented.

That said, `myrmica` will try to make a reasonable choice of run-time artifact
selections based on a set of Maven coordinates that you provide.

> NOTE: since "__reasonable__" has a very broad interpretation, please be sure
> to read these usage notes in their entirety.

### Installation

`TODO` - Maven artifacts coming to JCenter and Maven Central soon.

### Usage

```$java
String M2 = "https://repo.maven.apache.org/maven2/";
String localRepo = "/tmp/repo/";
Repository repo = new Repository(localRepo, M2);

Coordinates arrowJdbc = new Coordinates("org.apache.arrow", "arrow-jdbc", "0.12.0");
Map<Artifact, Path> binaries = repo.installRuntimeArtifactsAt(arrowJdbc);
```

## Motivation

So far, I have yet to find a minimalistic library which supports M2 artifact
resolution semantics. The libraries mentioned at the end of this document are
more specialized in execution scopes other than run-time. In addition, they tend
to bring in additional frameworks and dependencies of which I have very little
to no control of (as well as deep understanding).

Again, the purpose of `myrmica` is completely focused on the runtime execution
environment of an application.

## What's supported

- Maven style runtime dependency detection and installation into a local repository.
- `import` scoped `pom` dependencies (to support multiple inheritance.
- Gradle's __latest version wins__ default conflict resolution strategy.

## What's not supported

- Profile activations.
- Wildcard dependency exclusions.
- Version range selectors.
- `LATEST` version selector.
- Maven deployment metadata for `SNAPSHOT` artifacts.
- Artifact MD5/SHA checksum verification on downloaded artifacts.

Any Pull Requests to improve upon these points are more than welcome :).

A few recommendations:

- Use fixed version numbers.
- Try to publish uber-jar style shaded packages for larger applications (i.e. 
dependency resolution accuracy starts suffering once complex dependency trees
are specified).

## Similar projects

- [Apache Ivy](http://ant.apache.org/ivy/)
- [Eclipse/Apache Aether](https://projects.eclipse.org/projects/technology.aether)
- [ShrinkWrap Resolver](https://github.com/shrinkwrap/resolver)

# Disclaimer

> This project is not production ready, and still requires security and code correctness audits.
> You use this software at your own risk. Vaccove Crana, LLC., its affiliates and subsidiaries waive
> any and all liability for any damages caused to you by your usage of this software.
