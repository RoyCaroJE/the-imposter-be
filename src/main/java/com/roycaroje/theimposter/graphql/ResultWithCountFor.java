package com.roycaroje.theimposter.graphql;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResultWithCountFor<T> {
    private int queryCount;
    private List<T> result;

    public ResultWithCountFor(List<T> result){
        this.queryCount = result.size();
        this.result = result;
    }
}
