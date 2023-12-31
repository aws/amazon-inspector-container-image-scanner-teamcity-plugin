This plugin gives you the ability to add Amazon Inspector vulnerability scans to your pipeline. These scans produce detailed reports at the end of your build so you can investigate and remediate risk before deployment. These scans can also be configured to pass or fail pipeline executions based on the number and severity of vulnerabilities detected.

Amazon Inspector is a vulnerability management service offered by AWS that scans container images for both operating system and programming language package vulnerabilities based on CVEs.  For more information on Amazon Inspector’s CI/CD integration see [Integrating Amazon Inspector scans into your CI/CD pipeline](https://docs.aws.amazon.com/inspector/latest/user/scanning-cicd.html).

For a list of packages and container image formats the Inspector plugin supports see, [Supported packages and image formats](https://docs.aws.amazon.com/inspector/latest/user/sbom-generator.html#sbomgen-supported).

Follow the steps in each section of this document to use the Inspector TeamCity plugin:

1. Set up an AWS account

* Configure an AWS account with an IAM role that allows access to the Inspector SBOM scanning API. For instructions, see [Setting up an AWS account to use the Amazon Inspector CI/CD integration](https://docs.aws.amazon.com/inspector/latest/user/configure-cicd-account.html)

3. Install the Inspector TeamCity Plugin

1. From your dashboard, go to Administration > Plugins.
2. Search for Amazon Inspector Scans.
3. Install the plugin.

2. Install and configure the Inspector SBOM Generator

* Install and configure the Amazon Inspector SBOM Generator. For instructions, see [Installing Amazon Inspector SBOM Generator (Sbomgen)](https://docs.aws.amazon.com/inspector/latest/user/sbom-generator.html#install-sbomgen)

4. Add Inspector scan to your project

2. On the configuration page, choose Build Steps, click Add build step and select Amazon Inspector Scan.
3. Configure the Amazon inspector Scan build step by filling in following details:
    1. Add a Step name.
    2. For Image Id input the path to your image.
    3. For Path to SBOM generator Binary add the installation path to your Amazon Inspector SBOM Generator.
    4. For Role Arn the ARN for the role you configured in step 1.
    5. Select a Region to send the scan request through.
    6. For Docker Authentication add your Docker Username and Docker Password.
    7. [Optional] Specify the Maximum Vulnerabilities Allowed based on severity. If the maximum is exceeded during a scan the image build will fail. If the values are all 0 the build will succeed regardless of the number of vulnerabilities found.
4. Select Save.

5. View your Inspector scan report

1. Complete a new build of your project.
2. When the build completes select an output format from the results. When you select HTML you have the option to download a JSON SBOM or CSV version of the report.

### Known Limitations and Issues

* Support for Windows OS and macOS is not provided at this time.
* Sbomgen was load tested against container images spanning 5 GB in size, 60 layers, and 2,000 installed packages. Sbomgen should be able to inventory images of this size within 5 minutes; however, this may vary depending on the configuration of your image and available hardware resources.
* Sbomgen prioritizes accuracy and low false positive rates, which often comes at the expense of speed.
* Sbomgen ONLY generates SBOMs - it does not perform vulnerability identification at this time.
* Sbomgen only generates SBOMs in CycloneDX + JSON format at this time.

