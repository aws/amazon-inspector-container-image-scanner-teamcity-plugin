This plugin gives you the ability to add Amazon Inspector vulnerability scans to your pipeline. These scans produce detailed reports at the end of your build so you can investigate and remediate risk before deployment. These scans can also be configured to pass or fail pipeline executions based on the number and severity of vulnerabilities detected.

Amazon Inspector is a vulnerability management service offered by AWS that scans container images for both operating system and programming language package vulnerabilities based on CVEs. For more information on Amazon Inspector’s CI/CD integration, see [Integrating Amazon Inspector scans into your CI/CD pipeline](https://docs.aws.amazon.com/inspector/latest/user/scanning-cicd.html).

For a list of operating systems and programming languages Amazon Inspector supports, see [Supported operating systems and programming languages](https://docs.aws.amazon.com/inspector/latest/user/supported.html).

For a list of steps describing how to set up this plugin, see [Using the Amazon Inspector TeamCity plugin](https://docs.aws.amazon.com/inspector/latest/user/cicd-teamcity.html).