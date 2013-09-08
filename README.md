#Glass

##Android Parallax Background Library.




This is an Android library that allows you to easily create a parallax background for your Android app. This is a background that moves independently of your content and moves proportionately to the physical phone's gyration. 

##Getting Started
####Import Glass
Glass is a library project and should be added to your workspace in the same way you would add any other library project.
####Defining Your Layout
In order to user Glass, you need to be using a FrameLayout as your top layout element. In most situations you can just surround your existing layout with a FrameLayout.
```
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/glass"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity" >

  YOUR REGUALAR LAYOUT GOES IN HERE

</FrameLayout

```

####Java
In your onCreate
```Java
		FrameLayout fl = (FrameLayout) findViewById(R.id.glass);
		Drawable my_background = (Drawable) getResources().getDrawable(R.drawable.my_bg);
		Double scale_img = 4.0;
		g = new Glass(this,  fl, my_background, scale_img);
		g.dimLights();
		g.start();
```

Make sure you start and stop Glass with your app in onPause and onResume.
```Java
	@Override
	protected void onPause(){
		super.onPause();
		g.stop();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		g.start();
	}
```



