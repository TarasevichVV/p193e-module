job("MNTLAB-ykachatkou-main-build-job")
{
  description'main-job'
  
  parameters {
        choiceParam('BRANCH_NAME', ['ykachatkou', 'master'],'')
        activeChoiceParam('BUILDS_TRIGGER') {
            description('Available Options')
            choiceType('CHECKBOX')
            groovyScript {
                script('''
                    job_list=[]
                    for(i in (1..4)){
                      job_list.add("MNTLAB-ykachatkou-child${i}-build-job")
                    }
                    return job_list
                    ''')    
            }
        }
  }
  blockOnDownstreamProjects()
   
  steps {
        downstreamParameterized {
            trigger('$BUILDS_TRIGGER') {
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

for(i in 1..4){
    job("MNTLAB-ykachatkou-child${i}-build-job"){
        description "child-job ${i}"
        parameters {
            activeChoiceParam('BRANCH_NAME') {
                description('Available Options')
                choiceType('SINGLE_SELECT')
                groovyScript {
                   script('''
                       def git = ("git ls-remote -t -h https://github.com/MNT-Lab/d193l-module.git").execute()
                       list=[]
                       git.in.eachLine {line ->
                         list.add(line.split("/")[-1])
                       }
                       return list
                       ''')    
                }
            }
        }
        
        scm {
          git {
            remote {
                github('MNT-Lab/d193l-module')
            }
            branch('$BRANCH_NAME')
          }
        }
        steps {
          shell('chmod +x script.sh; ./script.sh > output.txt; tar czf ${BRANCH_NAME}_dsl_script.tar.gz output.txt')   
        }
        publishers {
          archiveArtifacts {
              pattern('${BRANCH_NAME}_dsl_script.tar.gz')
              pattern('output.txt')
              onlyIfSuccessful()
          }
        }
   
    }
}
