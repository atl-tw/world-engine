World Engine
============

Purpose
-------

This is a Gradle plugin for handling Terraform deployments in an opinionated way. It 
provides for auto-discovering and configuring environments based on naming strategies.

Concepts
--------

The Terraform code is broken up into a series of "components". Each component could be
an application/micro-service, or a set of common infrastructure, but at their core they
are a set of things that are going to get deployed together. You can configure Gradle tasks
to deploy each of the components with an environment and a version. These simply a naming convention
that you can use for configuration/naming and will select the variable files you need.

At each level of the terraform directory there is an "environments" folder, where you can put .tfvars 
files. These can be named by environment, or environment-version. They will then applied to the
terraform command in the order global environment, global version, component environment, component 
version.

Usage:
------

```groovy

plugins {
    id 'net.kebernet.world-engine'
}


task foo(type: WorldEngineTask){
    component = "application1"
    version = "blue"
    action = "plan"
}
```

Configuration
-------------

###WorldEngineTask

  * terraformExecutable (Optional): Path to the Terraform executable. 
    * Will default to .grade/terraform/terraform or PATH execution
    * Settable with property "we.terraformExecutable"
  * terraformSourceDir (Optional): Default src/deploy/terraform
  * component: The component to deploy.
    * Settable with property "we.component"
  * environment: The environment to deploy
    * Settable with property "we.environment"
  * version: The version to deploy.
    * Settable with property "we.version"
  * action: The terraform action to perform
  * failOnTerraformErrors (Optional): Default true; Will fail the build if Terraform reports 
    an error during execution
  * logTerraformOutput (Optional): Default false; Will echo Terraform output to the 
    gradle lifecycle log.
  * logDir (Optional): Default build/world-engine; Directory where logs will be written
  
###InstallTerraform
  * installTerraform: Default true; Allows you to prevent the task from executing
  * terraformVersion: Default "0.12.3"; The version of Terraform to download
  * terraformDownloadUrl (Optional): The full URL to the terraform download zip file.
    * You *NEED* to use this if you are not using x64 Window/Mac/Linux
  

Project Structure
-----------------

  * src
    * main
        * deploy
            * terraform <- root
                * environments <- global environment variable files
                    * dev.tfvars (example)
                    * qa.tfvars (example)
                    * prod.tfvars (example)
                * hooks <- executable files that are run around the TF tasks
                    * before.sh
                    * after.sh
                * components <- individual components
                    * [my component]
                        * main.tf
                        * environments <- component specific variable files
                            * dev.tfvars (example)
                            * qa.tfvars (example)
                            * prod-blue.tfvars (example)
                            * prod-green.tfvars (example)
                        * hooks <- executable files that are run around the TF tasks
                            * [task name]-before.sh
                            * [task name]-after.sh
                        
Terraform Install
-----------------

You can use the plugin to install terraform into the ```.gradle/terraform``` directory
using an "InstallTerraform" task

```groovy
task download(type:InstallTerraform){
    terraformVersion = "0.12.2" // <- optional, defaults to 0.12.3
    terraformDownloadUrl = "https://releases.hashicorp.com/terraform/0.12.3/terraform_0.12.2_solaris_amd64.zip"  
    // ^- this is important. It will only detect 64 bit versions for windows/mac/linux. If you are 
    // not using one of these, you need to specify the full download URL zip path 
    // also clobbers the terraformVersion property
}
```

Environments
------------

Environments are a two-part name separated by a "-" character. The first part is the environment name,
the second part is the environment version. 

For example, you might have:

  * terraform/environments/dev.tfvars
  * terraform/environments/dev-blue.tfvars
  * terraform/environments/dev-green.tfvars
  
This would be common if you are doing blue-green deployments. You have common values in the dev file, 
and values/overrides specific to each version in the blue/green files.

You can execute the scripts with ANY version of an environment. For example, if you are just deploying
a new version of your application with each build, you can use the build number as the version, and just
inherit the dev configuration.

Hooks
-----

Hooks are an easy way to invoke shell scripts around your terraform operations. If you have:

```groovy
task download(type:InstallTerraform){    
}

task foo(type:WorldEngineTask){
    component = "application1"
    action = "plan"
    dependsOn download // <-- if you have a terraform install task
}
```

You can create:
  * components/application1/hooks/foo-before.sh
  * components/application1/hooks/foo-after.sh
                        
These can be used to establish a state file from environment, or clean up data after a destroy. They
are executed as though they begin in the component root directory, and will share environment variables 
exported from the before script.                           