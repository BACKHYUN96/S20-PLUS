package app.purebit.session

/** Positions are transport metadata, never PCM transformations. EOF resumes from the beginning. */
object ResumeRules {
    fun position(savedMs:Long,durationMs:Long):Long=when {
        durationMs<=0||savedMs<0||savedMs>=durationMs->0
        else->savedMs
    }
    fun canCheckpoint(currentUri:String?,sourceUri:String,comparisonRate:Int,loading:Boolean,hasPcm:Boolean)=
        currentUri!=null && currentUri==sourceUri && sourceUri.startsWith("content://") && comparisonRate==0 && !loading && hasPcm
}
