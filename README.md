World Engine
============

Purpose
-------

This is a Gradle plugin for handling Terraform deployments in an opinionated way. It 
provides for auto-discovering and configuring environments based on naming strategies.

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


Project Structure
-----------------

  * src
    * main
        * deploy
            * terraform <- root
                * environments <- global environment variable files
                * components <- individual components
                    * [my component]
                        * main.tf
                        * environments <- component specific variable files
                        * hooks <- executable files that are run around the TF tasks
                        
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