job('MNTLAB-ashvedau-main-build-job') {
    description('Test run')
    parameters {
        choiceParam('BRANCH_NAME', ['ashvedau', 'master'])
        activeChoiceParam('EXECUTED_JOBS'){
            filterable()
            choiceType('CHECKBOX')
            groovyScript {
                script('''
                    def list = []
                    (1..4).each {
                        list.add("MNTLAB-ashvedau-child${it}-build-job")
                    }
                    return list
                ''')
            }
        }
    }
    blockOnDownstreamProjects()

    steps {
        downstreamParameterized {
            trigger('$EXECUTED_JOBS') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
                parameters {
                    predefinedProp('BRANCH_NAME', '$BRANCH_NAME')
                    }
                }
            }
        }
    }

for ( i in (1..4) ) {
    job("MNTLAB-ashvedau-child${i}-build-job") {
        description('Child job')

        parameters {
            activeChoiceParam('BRANCH_NAME') {
                description('Select job')
                choiceType('SINGLE_SELECT')
                groovyScript {
                    script('''
                def link = "https://github.com/MNT-Lab/d193l-module.git"
                def REMOT = "git ls-remote -h $link"
                def ECZEC = REMOT.execute()
                ECZEC.waitFor()
                def COLLECTS = ECZEC.in.text.readLines().collect{
                    it.split('/')[-1]
                    }
                    return COLLECTS
                    ''')
                }
            }
        }
        scm {
            git {
                remote {
                   name('branch')
                   url('https://github.com/MNT-Lab/d193l-module.git')
                }
                branch('$BRANCH_NAME')
                }
            }
        steps {
            shell ('''
            bash script.sh > output.txt
            tar czf ${BRANCH_NAME}_dsl_script.tar.gz jobs.groovy output.txt
            ''')
        }

        publishers {
            archiveArtifacts {
                pattern('${BRANCH_NAME}_dsl_script.tar.gz')
                onlyIfSuccessful()
            }
        }
    }
}