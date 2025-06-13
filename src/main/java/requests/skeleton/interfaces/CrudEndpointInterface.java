package requests.skeleton.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import models.BaseModel;

public interface CrudEndpointInterface {
    Object post(BaseModel model);
    Object get(int id) throws JsonProcessingException;
    Object getAll();
    Object update(BaseModel model);
    void delete(int id);

    }
