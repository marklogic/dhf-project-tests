This DHF 5.2.1 project is an End-2-End test project to run sanity tests in both Azure and AWS DHS. This project assigns users to existing roles, deploys Data Hub into DHS environment and runs flows.

## How to Use this Project
### Prerequisites
A provisioned DHS environment in Azure or AWS.

Has security admin credentials to deploy  users.

### Usage

git clone this repository to required branch

cd dhf-project-tests

cd dhs-test

edit gradle-[aws/azure]-properties file and 
add mlhost, mlUsername & mlPassword with hostname ,hubDeveloper user and password.

Execute: 

```./gradlew mldeployusers -PenvironmentName=aws -PmluserName=xxx -PmlPassword=yyy```

Then run tests  using the shell script

```./runTest.sh env hostname```






