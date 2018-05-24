package org.tensorflow.demo;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;


/**
 * Created by ASUS on 19/02/2017.
 */

public interface ApiService {

    @POST("/m2m/applications/Cultivo/containers/containerImagenes/contentInstances/")
    Call<ResponseMessage> saveIntereses(@Body Pedido pedido);

    @POST("/m2m/applications/Cultivo/containers/containerImagenes/contentInstances/")
    Call<ResponseMessage>  upload(@Body RequestBody updatedBody );

    @Multipart
    @POST("/m2m/applications/Cultivo/containers/containerImagenes/contentInstances/")
    Call<ResponseBody> postImage( @Part MultipartBody.Part image, @Part("name") RequestBody name); //@Part MultipartBody.Part image,


    class ResponseMessage {

        private String msg;

        public ResponseMessage(String msg){
            this.msg=msg;
        }

        public String getMsg() {
            return msg;
        }

    }


    class Pedido {

        private int idPlato;
        private String cliente;
        private String lugar;

        public Pedido(String cliente, String lugar, int idPlato){
            this.cliente = cliente;
            this.idPlato = idPlato;
            this.lugar = lugar;
        }

        public int getIdPlato(){ return idPlato;}
        public String getCliente(){ return cliente;}
        public String getLugar(){ return lugar;}


    }


}




