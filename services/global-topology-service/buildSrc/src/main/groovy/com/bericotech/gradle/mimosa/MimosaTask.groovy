package com.bericotech.gradle.mimosa

import org.gradle.api.tasks.Exec
import org.apache.tools.ant.taskdefs.condition.Os

class MimosaTask extends Exec{
	public final static NAME = 'mimosaTask';
	
    public MimosaTask( )
    {
        setGroup( "Mimosa" );
        setDependsOn( [] );
		setArgs( ["build", "-op"] as List );		
        determineExecutable();
    }

    private void determineExecutable( )
    {
        String mimosaExec = "mimosa";
        if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
        {
            mimosaExec = mimosaExec + ".cmd";
        }

        File localGrunt = project.file( "node_modules/mimosa/bin/${mimosaExec}" );
        if ( localGrunt.isFile() )
        {
            mimosaExec = localGrunt.toString();
        }

        setExecutable( mimosaExec );
    }

    @Override
    void setDependsOn( final Iterable<?> dependsOn )
    {
        List list = dependsOn as List;
        list.add( NpmInstallTask.NAME );

        super.setDependsOn( list )
    }
}