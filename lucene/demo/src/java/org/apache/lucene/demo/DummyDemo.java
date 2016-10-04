/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.demo;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class DummyDemo {
  
  public static void main(String[] args) throws IOException {

    Directory index = FSDirectory.open(Paths.get("x:/index"));
    IndexReader indexReader = DirectoryReader.open(index);
    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
    System.out.println("how many docs in title field? : " + indexReader.getDocCount("title"));
    System.out.println("leaves: " + indexReader.getContext().leaves().size());
   
    
    Term term1 = new Term("title", "java");
    Term term2 = new Term("title", "number");
    Term term3 = new Term("title", "please");
    Term term4 = new Term("title", "provides");
    Query query1 = new TermQuery(term1);
    Query query2 = new TermQuery(term2);
    Query query3 = new TermQuery(term3);
    Query query4 = new TermQuery(term4);
    PhraseQuery phraseQuery = new PhraseQuery("title", "task", "parallelism");
   // phraseQuery.add(new Term("title", "world"));
  //  phraseQuery.add(new Term("title", "war"));
  //  phraseQuery.add(new Term("title", "ii"));
    System.out.println(phraseQuery.toString());
    
  //  query1.createWeight(indexSearcher).explain(context, doc)
    System.out.println("term freqs: " + indexReader.docFreq(term1) + ", " + indexReader.docFreq(term2) + ", " + indexReader.docFreq(term3));
    
    BooleanQuery.Builder bqueryBuilder = new BooleanQuery.Builder();
    bqueryBuilder.add(query1, BooleanClause.Occur.MUST);
    bqueryBuilder.add(query2, BooleanClause.Occur.MUST);
    bqueryBuilder.add(query3, BooleanClause.Occur.MUST);
    bqueryBuilder.add(query4, BooleanClause.Occur.MUST);
    BooleanQuery bquery = bqueryBuilder.build();
    System.out.println("clauses: " + bquery.clauses());
    WildcardQuery wildCardQuery = new WildcardQuery(new Term("title", "war*"));
    
    Query rewrittenQuery = bquery.rewrite(indexReader);
    Set<Term> terms = new HashSet<Term>();
   System.out.println( rewrittenQuery.toString());
//    System.out.println("written type: " + ((ConstantScoreQuery)rewrittenQuery).toString());
    System.out.println("rewritten: " + rewrittenQuery.toString());
    System.out.println("rewritten class: " + rewrittenQuery.getClass().getCanonicalName());
  //  System.out.println("rewritten inner query: " + rewrittenQuery.getFilter());
    System.out.println("terms: " + terms);
    
    long t0 = System.currentTimeMillis();
    TopFieldDocs result = indexSearcher.search(phraseQuery, 10, Sort.RELEVANCE);
    ScoreDoc[] scoreDocs = result.scoreDocs;
    long t1 = System.currentTimeMillis();
    System.out.println("overall search time: " + (t1- t0));
    System.out.println("total hits: " + result.totalHits);
    System.out.println("scoreDocs length:" + scoreDocs.length);
    
/*    for(ScoreDoc scoreDoc : scoreDocs){
      Document doc = indexSearcher.doc(scoreDoc.doc);
      System.out.print(doc.get("id"));
      System.out.print(" : ");
      System.out.println(doc.get("title"));
    }*/
    
    indexReader.close();
    index.close();
    
    System.out.println("===============================");
  //  System.out.println("posting length for war: " + getPostingLength(new Term("title", "great")));
  
  }

  public static void write() throws IOException {
    Directory index = FSDirectory.open(Paths.get("x:/index"));
    IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
    IndexWriter indexWriter = new IndexWriter(index, config);
    
/*    FieldType fieldType = new FieldType();
    fieldType.setStored(true);
    fieldType.setOmitNorms(true);
    fieldType.setTokenized(true);*/
    Field titleField = new TextField("title", "Java 7 has a number of features that will please developers. Madhusudhan Konda provides an overview of these, including strings in switch statements, multi-catch exception handling, try-with-resource statements, the new File System API, extensions of the JVM, support for dynamically-typed languages, and the fork and join framework for task parallelism.", Store.YES);
    Field authorField = new TextField("author", "Madhusudhan Konda", Store.YES);
    
    Document doc = new Document();  
    
    doc.add(titleField);
    doc.add(authorField);
    
    indexWriter.addDocument(doc);
    indexWriter.commit();
    indexWriter.close();
    index.close();
    
    System.out.println("done");
  }
  

}
