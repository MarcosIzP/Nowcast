package com.tfg.nowcast

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.example.logansingscreen.data.manualdedatos.Song
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_music_test.*

class MusicTestActivity : AppCompatActivity(), Player.Listener {

    //VAriables

    lateinit var player:SimpleExoPlayer

    lateinit var playerView:PlayerControlView

    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("songs")

    lateinit var songItems:ArrayList<MediaItem>

    //creacion de un bundle que contiene el intent para poder recuperar los datos de email y proveedor
    val bundle:Bundle? = intent.extras
    val email = bundle?.getString("email")
    val provider = bundle?.getString("provider")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_test)

        //llamada de la funcion "setup" que se va a encargar de la acción de registro,
        // pero esta vez le pasamos los parámetros obtenidos de la otra actividad
        setup(email ?: "", provider ?: "")

        //Mediante lo siguiente, conseguirmos que la cuenta del ususario que inicia sesión se quede guardada.
        //Creacion de una constante que contendrá un archivo de tipo clave valor
        val preferencias = getSharedPreferences(getString(R.string.fichero_preferencias), Context.MODE_PRIVATE).edit()
        //Guradado de preferencias (email y proveedor)
        preferencias.putString("email", email)
        preferencias.putString("proveedor", provider)
        preferencias.apply()


        //Inicializamos la variable mediante "Builder()", que nos permite iniciarlizala sin necesidad de crearla como nuevo objeto
        player = SimpleExoPlayer.Builder(this).build()

        playerView = findViewById(R.id.playerview)
        songItems = ArrayList()

        getSongs()

    }

    //Autenticacion. Guardado de datos
    private fun setup(email: String, provider: String){

        exit_button.setOnClickListener {

            //Una vez se pulse el boton de cerrar sesion se eliminaran las preferencias guardadas

            val preferencias = getSharedPreferences(getString(R.string.fichero_preferencias), Context.MODE_PRIVATE).edit()
            preferencias.clear()
            preferencias.apply()

            //Llamada a los sevicios de firebase
            FirebaseAuth.getInstance().signOut()
            onBackPressed()
            //esta última línea sirve para una vez cerrada la sesión, volver a la pantalla de registro
        }
    }

    fun getSongs() {
        myRef.get().addOnSuccessListener {


            for (data in it.children) {
                var songAux = data.getValue(Song::class.java)

                if (songAux!=null) {

                    var metaData = MediaMetadata.Builder()
                        .setTitle(songAux.nombre)
                        .setArtist(songAux.artista)
                        .setAlbumTitle(songAux.album)
                        .setGenre(songAux.genero)
                        .setArtworkUri(Uri.parse(songAux.imageURL))
                        .build()

                    var item:MediaItem = MediaItem.Builder()
                        .setUri(songAux.songURL)
                        .setMediaMetadata(metaData)
                        .build()

                    songItems.add(item)
                }

            }
            initPlayer()
        }
    }

    private fun initPlayer() {
        player.repeatMode = Player.REPEAT_MODE_ALL
        player.addMediaItems(songItems)

        player.addListener(this)

        player.prepare()

        playerView.player = player
    }


    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)

        var nombre = findViewById<TextView>(R.id.song_name)
        var artista = findViewById<TextView>(R.id.artista)
        var album = findViewById<TextView>(R.id.album)
        var genero = findViewById<TextView>(R.id.genero)
        var imagen = findViewById<ImageView>(R.id.songimage_id)

        if(mediaItem!=null) {

            nombre.text = mediaItem.mediaMetadata.title
            artista.text = mediaItem.mediaMetadata.artist
            album.text = mediaItem.mediaMetadata.albumTitle
            genero.text = mediaItem.mediaMetadata.genre
            Picasso.get().load(mediaItem.mediaMetadata.artworkUri).into(imagen)

        }
    }
}

enum class ProviderType {
    //Tipo de autenticacion
//BASIC : autenticacion por email y contraseña
//GOOGLE : cuenta de google
    BASIC,
    GOOGLE

}