---
layout: model
title: Explain Clinical Doc ERA
author: John Snow Labs
name: explain_clinical_doc_era_en
date: 2020-08-19
tags: [pipeline, en, licensed]
article_header:
  type: cover
use_language_switcher: "Python-Scala-Java"
---

## Description
A pretrained pipeline with ner_clinical_events, assertion_dl and re_temporal_events_clinical. It will extract clinical entities, assign assertion status and find temporal relationships between clinical entities

{:.btn-box}
<button class="button button-orange" disabled>Live Demo</button>
[Open in Colab](https://github.com/JohnSnowLabs/spark-nlp-workshop/blob/master/tutorials/Certification_Trainings/Healthcare/11.Pretrained_Clinical_Pipelines.ipynb){:.button.button-orange.button-orange-trans.co.button-icon}{:target="_blank"}
[Download](https://s3.amazonaws.com/auxdata.johnsnowlabs.com/clinical/models/explain_clinical_doc_era_en_2.5.5_2.4_1597841630062.zip){:.button.button-orange.button-orange-trans.arr.button-icon}

## How to use

<div class="tabs-box" markdown="1">

{% include programmingLanguageSelectScalaPython.html %}

```python

pipeline = PretrainedPipeline('explain_clinical_doc_era', 'en', 'clinical/models')

annotations = pipeline.annotate(text)

annotations.keys()

```
```scala

pipeline = PretrainedPipeline('explain_clinical_doc_era', 'en', 'clinical/models')

annotations = pipeline.annotate(text)

annotations.keys()

```

</div>

{:.h2_title}
## Results
THe output is a dictionary with the following keys: 'sentences', 'clinical_ner_tags', 'clinical_ner_chunks_re', 'document', 'clinical_ner_chunks', 'assertion', 'clinical_relations', 'tokens', 'embeddings', 'pos_tags', 'dependencies'.


{:.model-param}
## Model Information

{:.table-model}
|---|---|
|Model Name:|explain_clinical_doc_era_en_2.5.5_2.4|
|Type:|pipeline|
|Compatibility:|Spark NLP 2.5.5|
|License:|Licensed|
|Edition:|Official|
|Language:|[en]|

{:.h2_title}
## Included Models 
 - ner_clinical_events
 - assertion_dl
 - re_temporal_events_clinical
 
