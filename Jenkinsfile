node('maven') {
   stage ('Checkout') {
       // Get the latest code from Gogs
       git url: 'http://gogs.pqc-support:3000/developer/person-qualification-calculator.git'
   }

   stage('Build') {

       // Update the version in the pom to match the jenkins build number.
       sh "mvn versions:set -DnewVersion=${env.BUILD_NUMBER} -DallowSnapshots=true"

       // Run the maven build
       sh "mvn clean package -DskipTests=false -Popenshift"

  }

  stage('Scan Code') {
      sh "mvn sonar:sonar -Dsonar.host.url=http://sonarqube-custom.pqc-support:9000 -DskipTests=false -Popenshift"
  }

   stage ('Archive') {

       // Archive the artifacts in jenkins
       step([$class: 'ArtifactArchiver', artifacts: '**/deploy/deployments/*.war', fingerprint: true])

       // Archive the test results.
       step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])

       // Deploy the artifacts to Nexus
       echo 'Sending artifacts to Nexus'
       sh "mvn deploy -Dmaven.test.skip=true -Popenshift -DaltDeploymentRepository=nexus::default::http://nexus.pqc-support:8081/repository/maven-releases/"
   }


   stage ('Create Image') {
       sh "echo 'Downloading WAR file from http://nexus:8081/nexus/repositroy/maven-releases/gov/irs/pqc/person-qualification-calculator/${env.BUILD_NUMBER}/person-qualification-calculator-${env.BUILD_NUMBER}.war'"
       sh "curl -O http://nexus.pqc-support:8081/repository/maven-releases/gov/irs/pqc/person-qualification-calculator/${env.BUILD_NUMBER}/person-qualification-calculator-${env.BUILD_NUMBER}.war"
       sh "mkdir -p deploy/deployments && mv person-qualification-calculator-${env.BUILD_NUMBER}.war deploy/deployments/ROOT.war"

       withCredentials([usernamePassword(credentialsId: 'jenkins-sa', passwordVariable: 'TOKEN', usernameVariable: 'USER')]) {
       sh "oc delete bc/pqc-dev is/pqc-dev --token=$TOKEN --namespace pqc-dev || true"
       sh "oc set triggers dc/pqc-dev --from-image=pqc-dev:latest --remove --token=$TOKEN --namespace=pqc-dev || true"
       sh "oc new-build --binary --image-stream=openshift/jboss-eap70-openshift:latest --to=pqc-dev:latest --namespace=pqc-dev --token=$TOKEN || true"
       //sh "oc new-build --binary --image-stream=openshift/jboss-eap70-openshift:1.5 --to=pqc-dev:latest --namespace=pqc-dev --token=$TOKEN || true"
       sh "oc start-build --from-dir=deploy bc/pqc-dev --follow --wait --namespace=pqc-dev --token=$TOKEN"
      }
   }

   stage ('Scan Image') {
     withCredentials([usernamePassword(credentialsId: 'jenkins-sa', passwordVariable: 'TOKEN', usernameVariable: 'USER')]) {
       echo 'Pulling the docker image locally'
       sh "sudo docker login -u ${USER} -p ${TOKEN} docker-registry.default:5000"
       sh "sudo docker pull docker-registry.default:5000/pqc-dev/pqc-dev:latest"
       SCAN_RESULT = sh([returnStatus: true, script: 'sudo oscap-docker image-cve docker-registry:5000/pqc-dev/pqc-dev:latest --report report.html | grep true'])
       publishHTML( target: [allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: '.', reportFiles: 'report.html', reportName: 'OpenSCAP CVE Scan Report'])

       if (SCAN_RESULT == 0) {
          timeout(time: 7, unit: 'DAYS') {
          echo "CVE(s) Detected!"
          input message: 'One or more CVEs were detected. Please review the OpenSCAP Report before proceeding.', submitter: 'admin'
        }
         }
          else echo "Passed Scan"
     }
   }

   stage ('Deploy to Dev') {
     withCredentials([usernamePassword(credentialsId: 'jenkins-sa', passwordVariable: 'TOKEN', usernameVariable: 'USER')]) {
        sh "oc new-app pqc-dev:latest --namespace=pqc-dev --token=$TOKEN || true"
        sh "oc set resources dc/pqc-dev --requests=cpu=500m,memory=256Mi --limits=cpu=2000m,memory=1Gi -n pqc-dev --token=$TOKEN || true"
        sh "oc set probe dc/pqc-dev --readiness --failure-threshold=3 --initial-delay-seconds=3 --period-seconds=5 --timeout-seconds=3 --get-url=http://:8080/rest/healthz -n pqc-dev --token=$TOKEN"
        sh "oc set probe dc/pqc-dev --liveness --failure-threshold=30 --period-seconds=10 --timeout-seconds=3 --initial-delay-seconds=300 --get-url=http://:8080/rest/healthz -n pqc-dev --token=$TOKEN"
        sh "oc expose svc/pqc-dev --namespace=pqc-dev --token=$TOKEN || true"
        sh "oc set triggers dc/pqc-dev --from-image=pqc-dev:latest --containers=pqc-dev --namespace=pqc-dev --token=$TOKEN"
        sh "oc rollout latest dc/pqc-dev --namespace=pqc-dev --token=$TOKEN || true"
        sh "oc tag pqc-dev/pqc-dev:latest pqc-dev/pqc-dev:${env.BUILD_NUMBER} --namespace=pqc-dev --token=$TOKEN"
        openshiftVerifyDeployment apiURL: 'https://kubernetes:443', authToken: '$TOKEN', depCfg: 'pqc-dev', namespace: 'pqc-dev', replicaCount: '1', verbose: 'false', verifyReplicaCount: 'true', waitTime: '120', waitUnit: 'sec'
      }
   }

   stage('Deploy to Test') {
       timeout(time: 7, unit: 'DAYS') {
         input message: 'Do you want to deploy PQC to Test?', submitter: 'admin'
        }
       echo 'Promoting container to Test Environment'
       withCredentials([usernamePassword(credentialsId: 'jenkins-sa', passwordVariable: 'TOKEN', usernameVariable: 'USER')]) {
         sh "oc tag pqc-dev/pqc-dev:${env.BUILD_NUMBER} pqc-test/pqc-test:${env.BUILD_NUMBER} --token=$TOKEN"
         sh "oc tag pqc-test/pqc-test:${env.BUILD_NUMBER} pqc-test/pqc-test:latest --token=$TOKEN"
         openshiftVerifyDeployment apiURL: 'https://kubernetes:443',authToken: '$TOKEN', depCfg: 'pqc-test', namespace: 'pqc-test', replicaCount: '1', verbose: 'false', verifyReplicaCount: 'true', waitTime: '120', waitUnit: 'sec'
       }
   }

   stage ('Deploy to Prod') {
       timeout(time: 7, unit: 'DAYS') {
         input message: 'Do you want to deploy PQC to Production?', submitter: 'admin'
        }
       echo 'Promoting container to Production Environment'
       withCredentials([usernamePassword(credentialsId: 'jenkins-sa', passwordVariable: 'TOKEN', usernameVariable: 'USER')]) {
         sh "oc tag pqc-test/pqc-test:${env.BUILD_NUMBER} pqc-prod/pqc-prod:${env.BUILD_NUMBER} --token=$TOKEN"
         sh "oc tag pqc-prod/pqc-prod:${env.BUILD_NUMBER} pqc-prod/pqc-prod:latest --token=$TOKEN"
         openshiftVerifyDeployment apiURL:  'https://kubernetes:443',authToken: '$TOKEN', depCfg: 'pqc-prod', namespace: 'pqc-prod', replicaCount: '1', verbose: 'false', verifyReplicaCount: 'true', waitTime: '120', waitUnit: 'sec'
       }
   }
}
