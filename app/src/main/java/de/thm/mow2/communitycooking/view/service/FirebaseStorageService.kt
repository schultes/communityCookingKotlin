package de.thm.mow2.communitycooking.view.service

import android.graphics.Bitmap
import android.widget.ImageView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream


class FirebaseStorageService {
    companion object {

        private const val STORAGE_DIR = "storage"

        fun uploadImageIntoStorage(imageId: String, image: Bitmap, callback: (Boolean) -> Unit) {
            val reference = Firebase.storage.getReference("$STORAGE_DIR/$imageId.jpg")

            val byteArrayOutputStream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream)
            val data = byteArrayOutputStream.toByteArray()

            reference.putBytes(data).addOnSuccessListener { callback(true) }.addOnFailureListener { callback(false) }
        }

        fun downloadImageIntoImageView(imageId: String, imageView: ImageView, placeholderResource: Int) {
            if (imageId.isNotEmpty()) {
                try {
                    Firebase.storage.getReference("$STORAGE_DIR/$imageId.jpg").downloadUrl
                        .addOnSuccessListener { uri ->
                            Picasso
                                .get()
                                .load(uri)
                                .placeholder(placeholderResource)
                                .error(placeholderResource)
                                .into(imageView)
                        }
                } catch (exception: Exception) {

                }
            } else {
                imageView.setImageResource(placeholderResource)
            }
        }
    }
}