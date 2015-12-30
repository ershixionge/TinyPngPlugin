package webp.plugin

import com.android.build.gradle.AppPlugin
import com.tinify.*
import org.gradle.api.Plugin
import org.gradle.api.Project

class WebpConvertBuildPlugin implements Plugin<Project> {


    @Override
    void apply(Project project) {

        def hasApp = project.plugins.withType(AppPlugin)

        def variants = hasApp ? project.android.applicationVariants : project.android.libraryVariants

        Tinify.setKey("wpS2bUFw3SQr6UmjHpJrhixKlPYmRL-F");

        int n = 0

        project.afterEvaluate {

            variants.all { variant ->
                def flavor = variant.getVariantData().getVariantConfiguration().getFlavorName()
                def buildType = variant.getVariantData().getVariantConfiguration().getBuildType().name
                def dx = project.tasks.findByName("process${variant.name.capitalize()}Resources")
                def webpConvertPlugin = "webpConvertPlugin${variant.name.capitalize()}"
                project.task(webpConvertPlugin) << {
                    String resPath = "${project.buildDir}/intermediates/res/${flavor}/${buildType}"
                    println "resPath:" + resPath
                    def dir = new File("${resPath}")
                    dir.eachDirMatch(~/drawable[a-z-]*/) { drawDir ->
                        println "drawableDir:" + drawDir
                        def file = new File("${drawDir}")
                        file.eachFile { filename ->
//                            println "filename:" + filename
                            def name = filename.name
                            def f = new File("${project.projectDir}/webp_white_list.txt")
                            def isInWhiteList = false
                            f.eachLine { whiteName ->
//                                println "find white list line: ${whiteName}"
                                if (name.equals(whiteName)) {
                                    isInWhiteList = true
                                }
                            }
                            if (!isInWhiteList) {
                                if (name.endsWith(".jpg") || name.endsWith(".png")) {
                                    if (!name.contains(".9")) {
                                        println "find target pic >>>>>>>>>>>>>" + name
                                        def picName = name.split('\\.')[0]
                                        def suffix = name.split('\\.')[1]
                                        println "picName:" + picName

                                        if (n < 5) {
                                           def tSource = Tinify.fromFile("${filename}");
                                            tSource.toFile("${drawDir}/${picName}tiny.${suffix}");
                                            n++
                                        }

//                                        "cwebp -q 75 -m 6 ${filename} -o ${drawDir}/${picName}.webp".execute()
//                                        sleep(10)
//                                        "rm ${filename}".execute()
//                                        println "delete:" + "${filename}"
//                                        println "generate:" + "${drawDir}/${picName}.webp"
                                    }
                                }
                            }

                        }
                    }
                }

                project.tasks.findByName(webpConvertPlugin).dependsOn dx.taskDependencies.getDependencies(dx)
                dx.dependsOn project.tasks.findByName(webpConvertPlugin)
            }

        }

    }
}