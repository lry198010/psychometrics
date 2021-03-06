/*
 * Copyright 2012 J. Patrick Meyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.psychometrics.measurement;

import com.itemanalysis.psychometrics.polycor.PearsonCorrelation;
import com.itemanalysis.psychometrics.polycor.PolyserialPlugin;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Formatter;

/**
 * This class computes statistics for individual item response options. It is used by the
 * ClassicalItem class in a classical item analysis.
 *
 * @author J. Patrick Meyer
 */
public class ItemStats {

    /**
     * The string or double value respresenting the value of the response option
     */
    private Object id = null;

    /**
     * Proportion endorsing item/category
     */
    private Mean mean = null;

    /**
     * Standard deviation of endorsed category
     */
    private StandardDeviation sd = null;

    /**
     * Item/Category - total correlation
     */
    private PearsonCorrelation pointBiserial = null;

    /**
     * Item/Category - total correlation
     */
    private PolyserialPlugin polyserial = null;

    /**
     * Use a biased corrected (n-1) standard deviation if true. Use baised (n) strandard deviation if not.
     */
    private boolean biasCorrection = true;

    /**
     * Use a Pearson type of correlation if true. Otherwise use a polyserial type of correlation.
     */
    private boolean pearson = true;

    public ItemStats(Object id, boolean biasCorrection, boolean pearson, boolean continuousItem){
        this.biasCorrection = biasCorrection;
        this.pearson = pearson;
        mean = new Mean();
        sd = new StandardDeviation();

        if(continuousItem) this.pearson = true;

        if(this.pearson){
            pointBiserial = new PearsonCorrelation();
        }else{
            polyserial = new PolyserialPlugin();
        }
    }

    /**
     * Incrementally update the item statistics
     *
     * @param testScore sum score on teh total test
     * @param itemScore score on teh individual item (or response option)
     */
    public void increment(double testScore, double itemScore){
        mean.increment(itemScore);
        sd.increment(itemScore);
        if(pearson){
            pointBiserial.increment(testScore, itemScore);
        }else{
            polyserial.increment(testScore, (int)itemScore);
        }
    }

    /**
     *
     * @return id of the response option
     */
    public Object getId(){
        return id;
    }

    public double getDifficulty(){
        return mean.getResult();
    }

    public double getStdDev(){
        return sd.getResult();
    }

    public double getDiscrimination(){
        if(pearson){
            if(biasCorrection) return pointBiserial.correctedValue();
            return pointBiserial.value();
        }else{
            if(biasCorrection) return polyserial.spuriousCorrectedValue();
            return polyserial.value();
        }

    }

    /**
     * A string that contains all of the item statistics.
     *
     * @return string of estimated statistics
     */
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        f.format("% 10.4f", getDifficulty()); f.format("%2s", " ");//category proportion endorsing
        f.format("% 10.4f", getStdDev()); f.format("%2s", " ");//category standard deviation
        f.format("% 10.4f", getDiscrimination());f.format("%2s", " ");  //item discrimination

        return f.toString();
    }

}
