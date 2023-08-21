# Solutions Maven Build Onboarding

The purpose of this project is to learn how to improve Maven builds with Gradle Enterprise.
We've set this up to have commonly encountered build issues. 
The full list of build issues this project has is written below.

Your starting point is the `main` branch. Good luck! :)

## List of issues

* Caching not enabled for goals that should be cacheable
* Volatile goal inputs
* Absolute paths as goal inputs

## Disclaimers

* Forked from the [spring-data-cassandra project](https://github.com/spring-projects/spring-data-cassandra) and modified for our needs
* [Original README](README-ORIGINAL.md)
* [Apache License](LICENSE.txt)

## Branches

* [main](https://github.com/tylerbertrand/spring-data-cassandra) - ([changes](https://github.com/tylerbertrand/spring-data-cassandra/compare/364efe5bcb1151c8efbcfa09e7f0f31c21ff0f0b...tylerbertrand:spring-data-cassandra:main)), which is set up with all the problems:
    * Caching not enabled for goals that should be cacheable
    * Volatile goal inputs
    * Absolute paths as goal inputs
* [onboarding-setup-fixes](https://github.com/tylerbertrand/spring-data-cassandra/tree/onboarding-setup-fixes) - ([changes](https://github.com/tylerbertrand/spring-data-cassandra/compare/main...tylerbertrand:spring-data-cassandra:onboarding-setup-fixes)), which fixes all of the above problems.
* [onboarding-ge-solutions](https://github.com/tylerbertrand/spring-data-cassandra/tree/onboarding-ge-solutions) - ([changes](https://github.com/spring-projects/spring-data-cassandra/compare/main...tylerbertrand:spring-data-cassandra:onboarding-ge-solutions)), follows the `main` branch but has a basic GE setup for the [Solutions GE instance](https://ge.solutions-team.gradle.com/scans)

## Contributing

When adding/modifying this project to include new issues, you should:

- Update the readme by specifying what problem was added in the `List of issues`, as well as in the `Branches` part specifying
  on which branch it was added or if a new branch was added and what was done there.
- After your changes are merged to `main`, you should merge `main` into [onboarding-ge-solutions](https://github.com/tylerbertrand/spring-data-cassandra/tree/onboarding-ge-solutions) and
  [onboarding-setup-fixes](https://github.com/tylerbertrand/spring-data-cassandra/tree/onboarding-setup-fixes)
- You should implement a fix of the new issue on the branch [onboarding-setup-fixes](https://github.com/tylerbertrand/spring-data-cassandra/tree/onboarding-setup-fixes).
