node('maven') {
  stage('Checkout') {
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

  stage('Archive') {

    // Archive the artifacts in jenkins
    step([$class: 'ArtifactArchiver', artifacts: '**/deployments/*.war', fingerprint: true])

    // Archive the test results.
    step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])

    // Deploy the artifacts to Nexus
    echo 'Sending artifacts to Nexus'
    sh "mvn deploy -Dmaven.test.skip=true -Popenshift -DaltDeploymentRepository=nexus::default::http://nexus.pqc-support:8081/repository/maven-releases/"
  }

  stage('Create Image') {
    echo 'Downloading WAR file from http://nexus.pqc-support:8081/repositroy/maven-releases/gov/irs/pqc/person-qualification-calculator/${env.BUILD_NUMBER}/person-qualification-calculator-${env.BUILD_NUMBER}.war'
    sh "curl -O http://nexus.pqc-support:8081/repository/maven-releases/gov/irs/pqc/person-qualification-calculator/${env.BUILD_NUMBER}/person-qualification-calculator-${env.BUILD_NUMBER}.war"

    withCredentials([usernamePassword(credentialsId: 'jenkins-sa', passwordVariable: 'TOKEN', usernameVariable: 'USER')]) {
      sh "oc delete bc/pqc-dev is/pqc-dev --token=$TOKEN --namespace pqc-dev || true"
      sh "oc rollout pause dc/pqc-dev --token=$TOKEN --namespace=pqc-dev || true"
      sh "oc new-build --binary --image-stream=openshift/jboss-eap70-openshift:latest --to=pqc-dev:latest --namespace=pqc-dev --token=$TOKEN || true"
      //sh "oc new-build --binary --image-stream=openshift/jboss-eap70-openshift:1.3 --to=pqc-dev:latest --namespace=pqc-dev --token=$TOKEN || true"
      sh "oc start-build --from-dir=deployments bc/pqc-dev --follow --wait --namespace=pqc-dev --token=$TOKEN"
    }
  }

}

podTemplate(name: 'scap', label: 'scap', cloud: 'openshift', containers: [
    containerTemplate(
        name: 'jnlp',
        image: 'docker-registry.default.svc:5000/openshift/scap-slave',
        alwaysPullImage: true,
        args: '${computer.jnlpmac} ${computer.name}',
        ttyEnabled: false,
        privileged: true,
        workingDir: '/tmp/jenkins')],
    volumes: [
        hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock')
    ]
) {
  node('scap') {
    stage('OpenSCAP Scan') {
      SCAN_RESULT = sh([returnStatus: true, script: 'image-inspector --image=docker-registry.default.svc:5000/pqc-dev/pqc-dev:latest --path=/tmp/image-content --scan-type=openscap --openscap-html-report | grep "0 failed" $(find /var/tmp -name results.html)'])
      sh 'mv $(find /var/tmp -name results.html) .'
      publishHTML(target: [allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: '.', reportFiles: 'results.html', reportName: 'OpenSCAP CVE Scan Results'])
      archiveArtifacts 'results.html'
      // In order to enable the CSS in the archived report, start Jenkins with: java -jar -Dhudson.model.DirectoryBrowserSupport.CSP="" jenkins.war
      if (SCAN_RESULT == 0) {
        timeout(time: 7, unit: 'DAYS') {
          echo "CVE(s) Detected!"
          input message: 'One or more CVEs were detected. Please review the OpenSCAP Report before proceeding.', submitter: 'admin'
        }
      } else echo "Passed Scan"
    }
  }
}

node('maven') {

  stage('Deploy to Dev') {
    withCredentials([usernamePassword(credentialsId: 'jenkins-sa', passwordVariable: 'TOKEN', usernameVariable: 'USER')]) {
      sh "oc rollout resume dc/pqc-dev --token=$TOKEN --namespace=pqc-dev || true"
      sh "oc tag pqc-dev/pqc-dev:latest pqc-dev/pqc-dev:${env.BUILD_NUMBER} --namespace=pqc-dev --token=$TOKEN"
      openshiftVerifyDeployment apiURL: 'https://kubernetes.default:443', authToken: '$TOKEN', depCfg: 'pqc-dev', namespace: 'pqc-dev', replicaCount: '1', verbose: 'false', verifyReplicaCount: 'true', waitTime: '120', waitUnit: 'sec'
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
      openshiftVerifyDeployment apiURL: 'https://kubernetes.default:443', authToken: '$TOKEN', depCfg: 'pqc-test', namespace: 'pqc-test', replicaCount: '1', verbose: 'false', verifyReplicaCount: 'true', waitTime: '120', waitUnit: 'sec'
    }
  }

  stage('Deploy to Prod') {
    timeout(time: 7, unit: 'DAYS') {
      input message: 'Do you want to deploy PQC to Production?', submitter: 'admin'
    }
    echo 'Promoting container to Production Environment'
    withCredentials([usernamePassword(credentialsId: 'jenkins-sa', passwordVariable: 'TOKEN', usernameVariable: 'USER')]) {
      sh "oc tag pqc-test/pqc-test:${env.BUILD_NUMBER} pqc-prod/pqc-prod:${env.BUILD_NUMBER} --token=$TOKEN"
      sh "oc tag pqc-prod/pqc-prod:${env.BUILD_NUMBER} pqc-prod/pqc-prod:latest --token=$TOKEN"
      openshiftVerifyDeployment apiURL: 'https://kubernetes.default:443', authToken: '$TOKEN', depCfg: 'pqc-prod', namespace: 'pqc-prod', replicaCount: '1', verbose: 'false', verifyReplicaCount: 'true', waitTime: '120', waitUnit: 'sec'
    }
  }
}
