/*
 * Copyright 2016 Dennis Vriend
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

import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.{Elem, NodeSeq, XML}

/**
 * See: http://liddellj.com/using-media-type-parameters-to-version-an-http-api/
 *
 * As a rule, when requesting `application/json` or `application/xml` you should return the latest version and should
 * be the same as the latest vendor media type.
 *
 * When a client requests a representation, using the vendor specific media type which includes a version, the API should
 * return that representation
 */

// marshalling containers (Value Objects)
//case class Docusin(name: String, age: String)

trait XmlMarshallers extends ScalaXmlSupport {
//  implicit def ec: ExecutionContext

  def marshalDocusignConnectResponse(response: DocusignConnectResponse): NodeSeq =
    <EnvelopeStatus>
      <EnvelopeID>
        { response.envelopeId }
      </EnvelopeID>
      <Status>
        { response.envelopeStatus }
      </Status>
    </EnvelopeStatus>

  def marshalDocusignConnectResponses(responses: Iterable[DocusignConnectResponse]) =
    <persons>
      { responses.map(marshalDocusignConnectResponse) }
    </persons>

  implicit def responsesXmlFormat = Marshaller.opaque[Iterable[DocusignConnectResponse], NodeSeq](marshalDocusignConnectResponses)

  implicit def responseXmlFormat = Marshaller.opaque[DocusignConnectResponse, NodeSeq](marshalDocusignConnectResponse)


  /**
   * From the Iterable[DocusignConnectResponse] value-object convert to a version and then marshal, wrap in an entity;
   * communicate with the VO in the API
   */
  implicit def xmlApplicationMarshallers: ToResponseMarshaller[Iterable[DocusignConnectResponse]] = Marshaller.oneOf(
    Marshaller.withOpenCharset(MediaTypes.`application/xml`) { (persons, charset) ⇒
      HttpResponse(entity =
        HttpEntity.CloseDelimited(
          ContentType.WithCharset(MediaTypes.`application/xml`, HttpCharsets.`UTF-8`),
          Source.fromIterator(() ⇒ persons.iterator).mapAsync(1) { response ⇒
            Marshal(persons.map(response ⇒ DocusignConnectResponse(response.envelopeId, response.envelopeStatus))).to[NodeSeq]
          }.map(ns ⇒ ByteString(ns.toString))
        ))
    }
  )

  implicit def xmlTextMarshallers: ToResponseMarshaller[Iterable[DocusignConnectResponse]] = Marshaller.oneOf(
    Marshaller.withOpenCharset(MediaTypes.`text/xml`) { (persons, charset) ⇒
      HttpResponse(entity =
        HttpEntity.CloseDelimited(
          ContentType.WithCharset(MediaTypes.`text/xml`, HttpCharsets.`UTF-8`),
          Source.fromIterator(() ⇒ persons.iterator).mapAsync(1) { response ⇒
            Marshal(persons.map(response ⇒ DocusignConnectResponse(response.envelopeId, response.envelopeStatus))).to[NodeSeq]
          }.map(ns ⇒ ByteString(ns.toString))
        ))
    }
  )

  /**
   * From the DocusignConnectResponse value-object convert to a version and then marshal, wrap in an entity;
   * communicate with the VO in the API
   */
  implicit def xmlApplicationMarshaller: ToResponseMarshaller[DocusignConnectResponse] = Marshaller.oneOf(
    Marshaller.withOpenCharset(MediaTypes.`application/xml`) { (response, charset) ⇒
      HttpResponse(entity =
        HttpEntity.CloseDelimited(
          ContentType.WithCharset(MediaTypes.`application/xml`, HttpCharsets.`UTF-8`),
          Source.fromFuture(Marshal(DocusignConnectResponse(response.envelopeId, response.envelopeStatus)).to[NodeSeq])
            .map(ns ⇒ ByteString(ns.toString))
        ))
    }
  )

  implicit def xmlTextMarshaller: ToResponseMarshaller[DocusignConnectResponse] = Marshaller.oneOf(
    Marshaller.withOpenCharset(MediaTypes.`text/xml`) { (response, charset) ⇒
      HttpResponse(entity =
        HttpEntity.CloseDelimited(
          ContentType.WithCharset(MediaTypes.`text/xml`, HttpCharsets.`UTF-8`),
          Source.fromFuture(Marshal(DocusignConnectResponse(response.envelopeId, response.envelopeStatus)).to[NodeSeq])
            .map(ns ⇒ ByteString(ns.toString))
        ))
    }
  )

  // curl -X POST -H "Content-Type: application/xml" -d '<response><name>John Doe</name><age>25</age><married>true</married></response>' localhost:8080/response
  def xmlApplicationEntityUnmarshaller(implicit mat: Materializer): FromEntityUnmarshaller[DocusignConnectResponse] =
    Unmarshaller.byteStringUnmarshaller.forContentTypes(MediaTypes.`application/xml`).mapWithCharset { (data, charset) ⇒
      val input: String = if (charset == HttpCharsets.`UTF-8`) data.utf8String else data.decodeString(charset.nioCharset.name)
      val xml: Elem = XML.loadString(input)
      val envelopeId: String = (xml \\ "DocuSignEnvelopeInformation" \\ "EnvelopeStatus" \\ "EnvelopeID" ).text
      val envelopeStatus: String = (xml \\ "DocuSignEnvelopeInformation" \\ "EnvelopeStatus" \\ "Status").text
      DocusignConnectResponse(envelopeId, envelopeStatus)
    }

  def xmlTextEntityUnmarshaller(implicit mat: Materializer): FromEntityUnmarshaller[DocusignConnectResponse] =
    Unmarshaller.byteStringUnmarshaller.forContentTypes(MediaTypes.`text/xml`).mapWithCharset { (data, charset) ⇒
      val input: String = if (charset == HttpCharsets.`UTF-8`) data.utf8String else data.decodeString(charset.nioCharset.name)
      val xml: Elem = XML.loadString(input)
      val envelopeId: String = (xml \\ "DocuSignEnvelopeInformation" \\ "EnvelopeStatus" \\ "EnvelopeID" ).text
      val envelopeStatus: String = (xml \\ "DocuSignEnvelopeInformation" \\ "EnvelopeStatus" \\ "Status").text
      DocusignConnectResponse(envelopeId, envelopeStatus)
    }

  // will be used by the unmarshallers above
  implicit def xmlUnmarshaller(implicit mat: Materializer): FromEntityUnmarshaller[DocusignConnectResponse] =
    Unmarshaller.firstOf[HttpEntity, DocusignConnectResponse](
      xmlApplicationEntityUnmarshaller, xmlTextEntityUnmarshaller
    )


}
