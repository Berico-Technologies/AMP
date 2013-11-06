package com.bericotech.gradle.mimosa

import org.gradle.api.Plugin
import org.gradle.api.Project


class MimosaPlugin implements Plugin<Project>{
	
	private Project project
	private MimosaPluginExtension pluginExtension
	
	@Override
	void apply( final Project project){
		project.extensions.create('mimosa', MimosaPluginExtension)
		this.project = project
		addNpmExecTask();
		addMimosaExecTask();

	}
	def addNpmExecTask(){
		def run = project.tasks.create(NpmInstallTask.NAME, NpmInstallTask)
		run.description = "Runs the npm install"
		run.group = "Mimosa"		
	}
	def addMimosaExecTask(){
		def run = project.tasks.create(MimosaTask.NAME, MimosaTask)
		run.description = "Runs mimosa"
		run.group = "Mimosa"
	}
}
