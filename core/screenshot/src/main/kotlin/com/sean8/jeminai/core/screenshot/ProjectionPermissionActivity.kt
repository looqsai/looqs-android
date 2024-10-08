//package com.sean8.jeminai.core.screenshot
//
//import android.app.Activity
//import android.app.Application
//import android.content.Context
//import android.content.Intent
//import android.media.projection.MediaProjectionManager
//import android.os.Bundle
//import com.sean8.jeminai.core.screenshot.ProjectionPermissionActivity.Companion
//
//class ProjectionPermissionActivity : Activity() {
//    private val activityResultProjection = 1
//
//    companion object {
//        var permissionResult: ((Intent?) -> Any)? = null
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        requestProjectionPermission()
//    }
//
//    private fun requestProjectionPermission() {
//        val mediaProjectionManager =
//            this.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager?
//        startActivityForResult(
//            mediaProjectionManager!!.createScreenCaptureIntent(),
//            activityResultProjection,
//        )
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        permissionResult?.invoke(
//            if (requestCode == activityResultProjection && resultCode == Activity.RESULT_OK) {
//                data!!
//            } else {
//                null
//            },
//        )
//
//        finish()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        permissionResult = null
//    }
//}
//
//fun Application.askProjection(result: (Intent?) -> Unit) {
//    Companion.permissionResult = result
//
//    val startActivity = Intent()
//    startActivity.setClass(this, ProjectionPermissionActivity::class.java)
//    startActivity.action = ProjectionPermissionActivity::class.java.name
//    startActivity.flags = (
//        Intent.FLAG_ACTIVITY_NEW_TASK
//            or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
//    this.startActivity(startActivity)
//}