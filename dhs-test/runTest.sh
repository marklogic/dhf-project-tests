#!/bin/bash
env=$1
host=$2

echo "Deploying users"
./gradlew mlDeployUsers -PenvironmentName=$env

echo "Running dhsDeploy as dh-dev user. This task will also run hubDeployAsDeveloper which deploys indexes, ELS configs and other ML resources. Check documentation for more info:
https://docs.marklogic.com/datahub/projects/deploy-to-cloud-services.html and https://docs.marklogic.com/datahub/security/users-and-roles.html"
./gradlew dhsDeploy -PenvironmentName=$env

#Get uri of a mapped document
uri=`curl -X GET --anyauth -u dh-op:dh-op https://$2:8010/v1/search?database=data-hub-FINAL&collection=map-concepts&pageLength=1 | xmllint --xpath "//@uri" - | cut -f 2 -d "="`
sed -e 's/^"//' -e 's/"$//' <<<"$uri"

#Getting the search response as a non pii user
AsANonPIIUser=`curl -X GET --anyauth -u dh-dev:dh-dev https://$2:8010/v1/documents?database=data-hub-FINAL&uri=$uri | xmllint --xpath "//source_concept_code" -`

#Getting the search response as a pii user
AsAPIIUser=`curl -X GET --anyauth -u pii-user:pii-user https://$2:8010/v1/documents?database=data-hub-FINAL&uri=$uri | xmllint --xpath "//source_concept_code" -`

if ! test -z "$AsANonPIIUser"
then
echo "TEST FAILED: A non pii user was able to read PII property""
fi

if test -z "$AsAPIIUser"
then
echo "TEST FAILED: A pii user was not able to read PII property"
fi
