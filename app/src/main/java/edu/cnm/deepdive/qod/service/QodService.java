package edu.cnm.deepdive.qod.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.cnm.deepdive.android.BaseFluentAsyncTask;
import edu.cnm.deepdive.qod.QodApplication;
import edu.cnm.deepdive.qod.R;
import edu.cnm.deepdive.qod.model.Quote;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface QodService {

  @GET("quotes/random")
  Call<Quote> get(@Header("Authorization") String formattedIdTokenString);

  class InstanceHolder {

    private static final QodService INSTANCE;

    static {
      QodApplication application = QodApplication.getInstance();
      Gson gson = new GsonBuilder()
          .excludeFieldsWithoutExposeAnnotation()
          .create();
      Retrofit retrofit = new Retrofit.Builder()
          .baseUrl(application.getApplicationContext().getString(R.string.base_url))
          .addConverterFactory(GsonConverterFactory.create(gson))
          .build();
      INSTANCE = retrofit.create(QodService.class);
    }

  }

  class GetQodTask extends BaseFluentAsyncTask<Void, Void, Quote, Quote> {

    private Quote quote;

    @Override
    protected Quote perform(Void... voids) throws TaskException {
      try {
        String token = QodApplication.getInstance().getString(R.string.authorization_value_format,
            GoogleSignInService.getInstance().getAccount().getIdToken());
        Response<Quote> response = InstanceHolder.INSTANCE.get(token).execute();
        if (!response.isSuccessful()) {
          throw new TaskException();
        }
        return response.body();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

  }


}
