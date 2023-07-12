# Android Custom Gallery Library

The Android Custom Gallery Library is a versatile and easy-to-use tool for implementing custom
gallery functionality to pick media in Android applications. This library provides developers with a
powerful set of features and components to create unique and engaging gallery experiences for their
users.

# Features

- Customizable Gallery Layout: The library offers a flexible and customizable gallery layout,
  allowing you to design the gallery view according to your specific requirements.

- You can customize the grid size, item spacing, and appearance to create a visually appealing and
  user-friendly gallery.

- Image Loading and Caching: The library handles efficient image loading and caching, ensuring
  smooth scrolling and optimal performance, even with large image collections.

- Zoom and Pan: Users can zoom in and out of images, as well as pan around to view different parts
  of a zoomed-in image. This feature provides an immersive and interactive viewing experience,
  especially for high-resolution images.

- Multiple Selection: The library supports multiple image selection, allowing users to select
  multiple images from the gallery. You can easily enable or disable this feature based on your
  app's requirements.

- Image Editing: This library has integrated features like cropping, rotating, applying filters, or
  adding text overlays to images within the gallery itself.

## Getting Started

To start using the Android Custom Gallery Library in your project, follow these steps:

- Clone this repository in your project.

- Initialize the library by calling of `CustomGallery.start()`

- The initiation requires an ActivityLauncher, context and an object of `CustomGalleryConfig`.

- `launcher` is the `ActivityResultLauncher` required to return the result of media picker.

```kotlin
CustomGallery.start(
    launcher,
    this@MainActivity,
    CustomGalleryConfig.Builder()
        .mediaTypes(listOf(IMAGE))
        .allowMultipleSelect(true)
        .isEditingEnabled(true)
        .build()
)
```

Now, the custom gallery is enabled in your app.
