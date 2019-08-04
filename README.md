## Blueberry

It should contain the following sub-system:
- Blueberry Manager 
- Blueberry Environment
- Crawling System
- Data Application Processing System

#### Blueberry Manager
A web manager system to manage data resources, schema and data analysis.

#### Blueberry Environment
Scripts to deploy a basic environment for Blueberry in Docker.
See more details: [blueberry-env](blueberry-env/README.md)

#### Crawling System
A sub-system for crawling data from resources and storage.

#### Data Application Processiong System
A sub-system for processing data.
In Data Application Processiong System, we may have various kinds of applications, e.g. news pushing, machine learning or monitoring.
It should provide pluginization interfaces to check in.
