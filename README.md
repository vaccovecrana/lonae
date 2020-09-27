# lonae

A lightweight Maven dependency resolver.

## Overview

[`lonae`](https://en.wikipedia.org/wiki/Myrmica_lonae) allows you to resolve runtime dependency graphs for Maven artifacts and
repositories from within your client code.

*Quick disclaimer*: To make the best use of your time (and mine):

> This project, although functional, is still in an experimental stage. If you
> are expecting to get artifacts resolved in the exact same way Maven
> or Gradle would, then `lonae` is not for you. At least not until solid
> support for runtime dependency semantic quirks is established/implemented.

That said, `lonae` will try to make a reasonable choice of run-time artifact
selections based on a set of Maven coordinates that you provide.

> NOTE: since "__reasonable__" has a very broad interpretation, please be sure
> to read these usage notes in their entirety.

### Installation

[ ![Download](https://api.bintray.com/packages/vaccovecrana/vacco-oss/lonae/images/download.svg) ](https://bintray.com/vaccovecrana/vacco-oss/lonae/_latestVersion)

Add the following dependency from JCenter to your project:

    implementation "io.vacco.myrmica:lonae:<VERSION>"

### Usage

```$java
MmRepository repo = new MmRepository("/tmp/repo", "https://repo1.maven.org/maven2/");

List<MmResolutionResult> jars = repo.installDefaultFrom(MmCoordinates.from("org.apache.arrow:arrow-jdbc:0.12.0"));
List<MmResolutionResult> sources = repo.installFrom(MmCoordinates.from("org.apache.arrow:arrow-jdbc:0.12.0"), "sources");
```

## Motivation

So far, I have yet to find a minimalistic library which supports M2 artifact
resolution semantics. The libraries mentioned at the end of this document are
more specialized in execution scopes other than run-time. They also bring in
additional frameworks and dependencies of which I have very little
to no control of (as well as a deep understanding of, for better or worse).

Again, the purpose of `lonae` is completely focused on the runtime artifact
retrieval for an application.

## What's supported

- [x] Maven style runtime dependency detection and installation (with optional classifiers) into a local repository.
- [x] `import` scoped `pom` dependencies (to support multiple inheritance.
- [x] Gradle's __latest version wins__ default conflict resolution strategy.

## What's left to do (Pull Requests welcomed and encouraged).

- [ ] Maven's __closest version wins__ default conflict resolution strategy.
- [ ] Adding dependencies from default active profile.
- [ ] Version range selectors.
- [ ] `LATEST` version selector.
- [ ] Maven deployment metadata for `SNAPSHOT` artifacts.
- [ ] Artifact MD5/SHA checksum verification on downloaded artifacts.

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
