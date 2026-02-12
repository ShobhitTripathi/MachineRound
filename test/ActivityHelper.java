package com.getourguide.interview.helpers;

import com.getourguide.interview.entity.Activity;
import com.getourguide.interview.entity.Supplier;

public class ActivityHelper {
    public static Activity createActivity(
        Long id,
        String title,
        int price,
        double rating,
        boolean specialOffer,
        Supplier supplier
    ) {
        var activity = new Activity();
        activity.setId(id);
        activity.setTitle(title);
        activity.setPrice(price);
        activity.setCurrency("EUR");
        activity.setRating(rating);
        activity.setSpecialOffer(specialOffer);
        activity.setSupplier(supplier);
        return activity;
    }
    
    public static Activity createActivity(
        Long id,
        String title,
        int price,
        String currency,
        double rating,
        boolean specialOffer,
        Supplier supplier
    ) {
        var activity = new Activity();
        activity.setId(id);
        activity.setTitle(title);
        activity.setPrice(price);
        activity.setCurrency(currency);
        activity.setRating(rating);
        activity.setSpecialOffer(specialOffer);
        activity.setSupplier(supplier);
        return activity;
    }
}
