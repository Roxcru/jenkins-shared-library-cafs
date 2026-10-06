def call(Map config = [:]) {

    def image = config.image
    def namespace = config.namespace ?: 'proy-final-cafs'
    def deployment = config.deployment ?: 'backend'
    def container = config.container ?: 'backend'

    if (!image) {
        error "Debe especificarse el parámetro 'image'"
    }

    withCredentials([
        file(
            credentialsId: 'doks-kubeconfig',
            variable: 'KUBECONFIG_FILE'
        )
    ]) {

        withEnv([
            "DEPLOY_IMAGE=${image}",
            "KUBE_NAMESPACE=${namespace}",
            "KUBE_DEPLOYMENT=${deployment}",
            "KUBE_CONTAINER=${container}"
        ]) {

            sh '''
                set -eu

                echo "=== Verificando Kubernetes ==="

                kubectl \
                    --kubeconfig "$KUBECONFIG_FILE" \
                    get nodes

                echo "=== Actualizando imagen ==="

                kubectl \
                    --kubeconfig "$KUBECONFIG_FILE" \
                    -n "$KUBE_NAMESPACE" \
                    set image deployment/"$KUBE_DEPLOYMENT" \
                    "$KUBE_CONTAINER=$DEPLOY_IMAGE"

                echo "=== Esperando rollout ==="

                kubectl \
                    --kubeconfig "$KUBECONFIG_FILE" \
                    -n "$KUBE_NAMESPACE" \
                    rollout status deployment/"$KUBE_DEPLOYMENT" \
                    --timeout=180s

                echo "=== Pods ==="

                kubectl \
                    --kubeconfig "$KUBECONFIG_FILE" \
                    get pods \
                    -n "$KUBE_NAMESPACE"
            '''
        }
    }
}