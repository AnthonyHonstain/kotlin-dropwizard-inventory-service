package honstain

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.util.*
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter

class ProvenanceIDFilter : ContainerRequestFilter {

    private val log: Logger = LoggerFactory.getLogger(ProvenanceIDFilter::class.java)

    override fun filter(requestContext: ContainerRequestContext) {
        MDC.clear()

        val rawProvenanceId = requestContext.getHeaderString("ProvenanceID")
        if (rawProvenanceId.isNullOrEmpty()) {
            val provenanceId = UUID.randomUUID()
            MDC.put("ProvenanceID", provenanceId.toString())
            //log.info("Created new provenanceId:$provenanceId")
        } else {
            MDC.put("ProvenanceID", rawProvenanceId)
            //log.info("Found existing provenanceId:rawProvenanceId")
        }
    }
}