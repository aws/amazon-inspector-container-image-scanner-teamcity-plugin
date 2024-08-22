import argparse
import sys
import requests
from requests.auth import HTTPBasicAuth
import time
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()

parser.add_argument('-a')    
parser.add_argument('-p')  
parser.add_argument('-u') 
parser.add_argument('-n')

args = parser.parse_args()

url = args.a
username = args.u
password = args.p
buildName = args.n

if "http://" not in url:
    url = "http://" + url
print(url)

def check_status_code(response):
    if response.status_code != 200:
            print("Recieved non-ok status, aborting")
            print(response.text)
            sys.exit(2)

def start_build():
    print("Starting build.")
    headers = {
        'Accept': 'application/json',
        'Content-Type': 'application/xml',
        'Host': 'teamcity.host.io',
        'User-Agent': 'my-app/1.0'
    }

    payload = f"""
    <build branchName="refs/heads/master">
        <triggeringOptions cleanSources="true" rebuildAllDependencies="false" queueAtTop="false"/>
        <buildType id="{buildName}"/>
        <properties>
            <property name="env.startedBy" value="build was triggering from %teamcity.serverUrl%/viewLog.html?buildId=%teamcity.build.id%"/>
        </properties>
    </build>
    """
    
        
    response = requests.post(
        url + "/app/rest/buildQueue",
        auth=HTTPBasicAuth(username, password),
        headers=headers,
        data=payload
    )

    check_status_code(response)

def check_if_build_finished():
    print("Waiting until build is finished. ")
    buildId = None
    while True:
        response = requests.get(
            url + "/app/rest/builds",
            auth=HTTPBasicAuth(username, password),
            params={'locator': f'buildType:{buildName},state:running'}
        )
        
        check_status_code(response)

        root = ET.fromstring(response.content)
        print(root.attrib)
        if buildId == None:
            buildId = root[0].attrib['id']
            print("Got buildId: " + buildId)

        if (root.attrib["count"] == '0'):
            return buildId

        print("Build still running, sleeping for 30 seconds.")
        time.sleep(30)

def did_build_pass(buildId):
    response = requests.get(
        url + "/app/rest/builds",
        auth=HTTPBasicAuth(username, password),
        params={'locator': f'buildType:{buildName},id:{buildId}'}
    )
    
    check_status_code(response)

    root = ET.fromstring(response.content)
    return root[0].attrib['status'] == 'SUCCESS'

start_build()
buildId = check_if_build_finished()
buildPassed = did_build_pass(buildId)

if (not buildPassed):
    sys.exit(1)
