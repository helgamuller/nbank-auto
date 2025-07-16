package api.requests.skeleton.interfaces;

import api.models.BaseModel;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface CrudEndpointInterface {
    Object post(BaseModel model);
    Object get(int id) throws JsonProcessingException;
    Object getAll();
    Object update(BaseModel model);
    void delete(int id);

    }
