package com.bericotech.gradle.mimosa

import org.gradle.api.tasks.Exec

class NpmInstallTask extends Exec{
    public final static NAME = 'npmInstall';

    public NpmInstallTask( )
    {
        setGroup( "Mimosa" );
        setExecutable( "npm" );
        setArgs( ["install", "mimosa"] as List );        
        getOutputs().dir( "node_modules" );
    }
}