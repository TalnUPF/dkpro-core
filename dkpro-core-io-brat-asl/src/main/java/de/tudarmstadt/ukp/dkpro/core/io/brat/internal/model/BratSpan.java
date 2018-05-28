/*
 * Copyright 2018
 * TALN Group UPF
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 package de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model;

import java.util.LinkedList;

import org.apache.uima.cas.text.AnnotationFS;

public class BratSpan
{
  public String elem;
  public String color;
  public String color_elem;
  public double min;
  public double max;
  public AnnotationFS fs;
  public LinkedList<Annotation> annotations; 
  
  
  public class Annotation {
      public Double weight;
      public BratTextAnnotation anno;
      public Annotation(Double weight2, BratTextAnnotation anno2, AnnotationFS aFS)
    {
        weight=weight2;
        anno=anno2;
        fs=aFS;
    }
 }
  

  
  public BratSpan(String elem, String color, String color_elem)
{
    super();
    this.elem = elem;
    this.color = color;
    this.color_elem = color_elem;
    this.min = Double.MAX_VALUE;
    this.max = Double.MIN_VALUE;
    annotations=new LinkedList<>();
}
  
public void reset(){
    this.min = Double.MAX_VALUE;
    this.max = Double.MIN_VALUE;
    annotations=new LinkedList<>();
}
public double getMin()
{
    return min;
}
public void setMin(double min)
{
    if (min<this.min) this.min = min;
}
public double getMax()
{
    return max;
}
public void setMax(double max)
{
    if (max>this.max) this.max = max;
}
public void addSpan(Double weight, BratTextAnnotation anno, AnnotationFS aFS)
{
    setMax(weight);
    setMin(weight);
    Annotation a= new Annotation(weight,anno,aFS);
    annotations.add(a);
    
    
}
  
}
