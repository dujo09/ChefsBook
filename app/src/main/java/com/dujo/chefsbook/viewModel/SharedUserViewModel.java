package com.dujo.chefsbook.viewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.dujo.chefsbook.data.model.User;
import com.dujo.chefsbook.data.repository.UserRepository;

public class SharedUserViewModel extends AndroidViewModel {
  private final UserRepository repo;
  private final LiveData<User> user;

  public SharedUserViewModel(@NonNull Application application) {
    super(application);
    repo = UserRepository.getInstance(application);
    user = repo.getUserLive();
  }

  public LiveData<User> getUser() {
    return user;
  }

  public void refresh() {
    repo.refreshFromServer();
  }

  public void clear() {
    repo.clearCache();
  }
}
