def call(Map config = [:]) {

    def imageName = config.image
    def imageTag = config.tag ?: env.BUILD_NUMBER

    if (!imageName) {
        error "Debe especificarse el parámetro 'image'"
    }

    withCredentials([
        usernamePassword(
            credentialsId: 'dockerhub-credentials',
            usernameVariable: 'DOCKERHUB_USER',
            passwordVariable: 'DOCKERHUB_TOKEN'
        )
    ]) {

        withEnv([
            "IMAGE_NAME=${imageName}",
            "IMAGE_TAG=${imageTag}"
        ]) {

            sh '''
                set -eu

                echo "$DOCKERHUB_TOKEN" | docker login \
                    -u "$DOCKERHUB_USER" \
                    --password-stdin

                docker build \
                    -t "$IMAGE_NAME:$IMAGE_TAG" \
                    -t "$IMAGE_NAME:latest" \
                    .

                docker push "$IMAGE_NAME:$IMAGE_TAG"
                docker push "$IMAGE_NAME:latest"

                docker logout
            '''
        }
    }
}